# Redis refresh: script and test case

Redis 缓存数据刷新的 Lua 脚本及相关测试用例。

测试时需启动 Redis server，并使用 redis-cli 来运行相关测试用例。

**环境要求**：Redis 3.2+



## LOCK

### 程序逻辑

```lua
local sid = redis.call('GET', KEYS[1]); 
if not sid or sid == ARGV[1] then 
    redis.call('PSETEX', KEYS[1], ARGV[2], ARGV[1]); 
    return 1; 
else 
    return 0; 
end;
```

### 测试代码

```lua
local exists = redis.call('EXISTS', KEYS[1]);
if (exists == 0) then
    error('key does not exists', 0);
end;
local ttl = redis.call('pttl', KEYS[1]);
if (ttl <= 0) then 
    if (ttl == -1) then
        error('the key has no associated expire.' .. ttl, 0);
    elseif (ttl == -2) then
        error('the key does not exist' .. ttl, 0);
    else
        error('ttl set failed' .. ttl, 0);
    end;
end;
return ttl;
```

### 测试步骤

```shell
# 1.删除 key：refreshLock（初始测试环境，避免数据污染）
del refreshLock

# 2.加锁
eval "local sid = redis.call('GET', KEYS[1]); if not sid or sid == ARGV[1] then redis.call('PSETEX', KEYS[1], ARGV[2], ARGV[1]); return 1; else return 0; end;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec 1000000

# 3.测试锁是否存在
eval "local exists = redis.call('EXISTS', KEYS[1]); if (exists == 0) then error('key does not exists', 0); end; local ttl = redis.call('pttl', KEYS[1]); if (ttl <= 0) then if (ttl == -1) then error('the key has no associated expire.' .. ttl, 0); elseif (ttl == -2) then error('the key does not exist' .. ttl, 0); else error('ttl set failed' .. ttl, 0); end; end; return ttl;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec
```



## UNLOCK

### 程序逻辑

```lua
local sid = redis.call('GET', KEYS[1]);
if not sid then 
    return 1; 
end; 
if sid == ARGV[1] then 
    redis.call('DEL', KEYS[1]); 
    return 1; 
end; 
return 0;
```

### 测试代码

```lua
local exists = redis.call('EXISTS', KEYS[1]);
if (exists > 0) then
    error('key delete failed', 0);
end;
return exists;
```

### 测试步骤

```shell
# 1~3：先运行 LOCK 测试过程（添加相关数据并确认数据存在）

# 4.执行释放锁逻辑
eval "local sid = redis.call('GET', KEYS[1]); if not sid then return 1; end; if sid == ARGV[1] then redis.call('DEL', KEYS[1]); return 1; end; return 0;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec

# 5.测试释放锁是否成功
eval "local exists = redis.call('EXISTS', KEYS[1]); if (exists > 0) then error('key delete failed', 0); end; return exists;" 1 refreshLock
```



## PUT

### 程序逻辑

```lua
-- 对于 Redis 版本 [3.2, 7.0) ，因为使用了随机命令“TIME”，因此特别声明使用“命令复制模式”
redis.replicate_commands();
local key = KEYS[1]; 
-- 获取服务器当前时间 [unixtime, microseconds]
local server_time = redis.call('time');
-- 时间数组转换为毫秒
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000));
-- 计算出下次刷新时间
local refresh_time = now + ARGV[1];
-- 循环添加数据到指定的 SortedSet（不要用 unpack，数据量稍大会异常，但主从模式下会传输很多命令）
for i = 2, #(ARGV) do 
    redis.call('zadd', key, refresh_time, ARGV[i]); 
end; 
-- 返回下次刷新时间，用于客户端判断是否成功执行
return refresh_time;
```



### 测试代码

```lua
local members = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES', 'LIMIT', ARGV[3], ARGV[4]);
local cnt = 0;
local score = 0;
local server_time = redis.call('time');
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000));
for i, value in ipairs(members) do
    local val = tostring(value);
    if ((val == 'a1') or (val == 'b1') or (val == 'c1')) then
        cnt = cnt + 1;
    else
        score = tonumber(value);
        if (score < now) then
            error('zadd failed, score:' .. score, 0);
        end;
    end;
end;
if (cnt < 3) then
    error('zadd failed, cnt:' .. cnt, 0);
end;
return {cnt, score};
```

### 测试步骤

```shell
# 1.先删除 key：refresh（初始测试环境，避免数据污染）
del refresh

# 2.添加 member：a1 b1 c1
eval "redis.replicate_commands(); local key = KEYS[1]; local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); local refresh_time = now + ARGV[1]; for i = 2, #(ARGV) do redis.call('zadd', key, refresh_time, ARGV[i]); end; return refresh_time;" 1 refresh 1000000 a1 b1 c1

# 3.判断 members 是否存在，分数是否大于当前时间
eval "local members = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES', 'LIMIT', ARGV[3], ARGV[4]); local cnt = 0; local score = 0; local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); for i, value in ipairs(members) do local val = tostring(value); if ((val == 'a1') or (val == 'b1') or (val == 'c1')) then cnt = cnt + 1; else score = tonumber(value); if (score < now) then error('zadd failed, score:' .. score, 0); end; end; end; if (cnt < 3) then error('zadd failed, cnt:' .. cnt, 0); end; return {cnt, score};" 1 refresh 0 2734676370672 0 100
```



## GET_UPDATE_REFRESH_MEMBERS

### 程序逻辑

```lua
redis.replicate_commands(); 
local key = KEYS[1];
local server_time = redis.call('time');
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000));
local refresh_time = now + ARGV[1];
local members = redis.call('ZRANGEBYSCORE', key, 0, refresh_time, 'LIMIT', 0, ARGV[2]);
for i = 1, #(members) do
    redis.call('zadd', key, refresh_time, members[i]);
end;
return members;
```

