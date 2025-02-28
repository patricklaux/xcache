# Redis refresh: script and test case

Redis 缓存数据刷新的 Lua 脚本及相关测试用例。

测试时需启动 Redis server，并使用 redis-cli 来运行相关测试用例。

**环境要求**：Redis 3.2+



## LOCK

### 程序逻辑

```lua
redis.call('hdel', KEYS[1], ARGV[1]); 
if (redis.call('hlen', KEYS[1]) == 0) then 
    redis.call('hset', KEYS[1], ARGV[1], 1); 
    return redis.call('pexpire', KEYS[1], ARGV[2]); 
end; 
return 0;
```

### 测试代码

```lua
local exists = redis.call('HEXISTS', KEYS[1], ARGV[1]);
if (exists == 0) then
    error('field does not exists', 0);
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
# 1.删除 key-field：refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec（初始测试环境，避免数据污染）
hdel refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec

# 2.加锁
eval "redis.call('hdel', KEYS[1], ARGV[1]); if (redis.call('hlen', KEYS[1]) == 0) then redis.call('hset', KEYS[1], ARGV[1], 1); return redis.call('pexpire', KEYS[1], ARGV[2]); end; return 0;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec 1000000

# 3.测试锁是否存在
eval "local exists = redis.call('HEXISTS', KEYS[1], ARGV[1]); if (exists == 0) then error('field does not exists', 0); end; local ttl = redis.call('pttl', KEYS[1]); if (ttl <= 0) then if (ttl == -1) then error('the key has no associated expire.' .. ttl, 0); elseif (ttl == -2) then error('the key does not exist' .. ttl, 0); else error('ttl set failed' .. ttl, 0); end; end; return ttl;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec
```



## UNLOCK

### 程序逻辑

```lua
redis.call('hdel', KEYS[1], ARGV[1]);
return redis.call('hlen', KEYS[1]);
```

### 测试代码

```lua
local exists = redis.call('HEXISTS', KEYS[1], ARGV[1]);
if (exists > 0) then
    error('field delete failed', 0);
end;
return exists;
```

### 测试步骤

```shell
# 1~3：先运行 LOCK 测试过程（添加相关数据并确认数据存在）

# 4.执行释放锁逻辑
eval "redis.call('hdel', KEYS[1], ARGV[1]); return redis.call('hlen', KEYS[1]);" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec

# 5.测试释放锁是否成功
eval "local exists = redis.call('HEXISTS', KEYS[1], ARGV[1]); if (exists > 0) then error('field delete failed', 0); end; return exists;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec
```



## LOCK_NEW_EXPIRE

### 程序逻辑

```lua
redis.call('hset', KEYS[1], ARGV[1], 1); 
return redis.call('pexpire', KEYS[1], ARGV[2]); 
```

### 测试代码

```lua
-- 与加锁测试代码相同
```

### 测试步骤

```shell
# 1.删除 key-field：refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec（初始测试环境，避免数据污染）
hdel refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec

# 2.锁续期
eval "redis.call('hset', KEYS[1], ARGV[1], 1); return redis.call('pexpire', KEYS[1], ARGV[2]);" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec 1000000

# 3.测试锁是否存在
eval "local exists = redis.call('HEXISTS', KEYS[1], ARGV[1]); if (exists == 0) then error('field does not exists', 0); end; local ttl = redis.call('pttl', KEYS[1]); if (ttl <= 0) then if (ttl == -1) then error('the key has no associated expire.' .. ttl, 0); elseif (ttl == -2) then error('the key does not exist' .. ttl, 0); else error('ttl set failed' .. ttl, 0); end; end; return ttl;" 1 refreshLock 29ac5767-3dbd-4586-906a-21ab83f036ec
```



## UPDATE_TASK_TIME

### 程序逻辑

```lua
redis.replicate_commands(); 
local server_time = redis.call('time'); 
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); 
local task_time = now + ARGV[1]; 
if (task_time > 0) then 
    redis.call('set', KEYS[1], task_time); 
end; 
return task_time;
```

### 测试代码

```lua
if (redis.call('exists', KEYS[1]) == 0) then
    error('key does not exists', 0);
end;
local server_time = redis.call('time'); 
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); 
local task_time = redis.call('get', KEYS[1]);
if (tonumber(task_time) <= now) then
    error('task_time set failed, task_time:' .. task_time, 0);
end;
return task_time;
```

### 测试步骤

```shell
# 1.删除 key：refreshPeriod（初始测试环境，避免数据污染）
del refreshPeriod

# 2.更新任务时间
eval "redis.replicate_commands(); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); local task_time = now + ARGV[1]; if (task_time > 0) then redis.call('set', KEYS[1], task_time); end; return task_time;" 1 refreshPeriod 1000000

# 3.测试任务时间是否存在
eval "if (redis.call('exists', KEYS[1]) == 0) then error('key does not exists', 0); end; local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); local task_time = redis.call('get', KEYS[1]); if (tonumber(task_time) <= now) then error('task_time set failed, task_time:' .. task_time, 0); end; return task_time;" 1 refreshPeriod
```



## ARRIVED_TASK_TIME

### 程序逻辑

```lua
if (redis.call('exists', KEYS[1]) == 0) then 
    return 1; 
end; 
local task_time = tonumber(redis.call('get', KEYS[1])); 
local server_time = redis.call('time'); 
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); 
if (now >= task_time) then 
    return 1; 
end; 
return 0;
```

### 测试代码

```lua
-- 无
```

### 测试步骤

```shell
# 1.删除 key：refreshPeriod（初始测试环境，避免数据污染）
del refreshPeriod

# 2.执行是否到达任务时间代码，观察返回值是否为 1
# 返回 1 表示首次执行任务
eval "if (redis.call('exists', KEYS[1]) == 0) then return 1; end; local task_time = tonumber(redis.call('get', KEYS[1])); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); if (now >= task_time) then return 1; end; return 0;" 1 refreshPeriod

# 3.更新为一个极短的任务时间（10 毫秒）
eval "redis.replicate_commands(); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); local task_time = now + ARGV[1]; if (task_time > 0) then redis.call('set', KEYS[1], task_time); end; return task_time;" 1 refreshPeriod 10

# 4. 执行是否到达任务时间代码，观察返回值是否为 1
# 返回 1 表示已到达任务时间
eval "if (redis.call('exists', KEYS[1]) == 0) then return 1; end; local task_time = tonumber(redis.call('get', KEYS[1])); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); if (now >= task_time) then return 1; end; return 0;" 1 refreshPeriod

# 5.更新为一个较长的任务时间（1000 秒）
eval "redis.replicate_commands(); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); local task_time = now + ARGV[1]; if (task_time > 0) then redis.call('set', KEYS[1], task_time); end; return task_time;" 1 refreshPeriod 1000000

# 6. 执行是否到达任务时间代码，观察返回值是否为 0
# 返回 0 表示未到达任务时间
eval "if (redis.call('exists', KEYS[1]) == 0) then return 1; end; local task_time = tonumber(redis.call('get', KEYS[1])); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); if (now >= task_time) then return 1; end; return 0;" 1 refreshPeriod
```



## PUT

### 程序逻辑

#### 分开成多个命令
```lua
-- 对于 Redis 版本 [3.2, 7.0) ，因为使用了随机命令“TIME”，因此特别声明使用“命令复制模式”
redis.replicate_commands();
-- 获取服务器当前时间 [unixtime, microseconds]
local server_time = redis.call('time');
-- 时间数组转换为毫秒
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000));
-- 计算出下次刷新时间
local refresh_time = now + ARGV[1];
-- 只有当刷新时间大于 0，才添加到指定的 SortedSet
if (refresh_time > 0) then
    for i = 2,#(ARGV) do
        redis.call('zadd', KEYS[1], refresh_time, ARGV[i]);
    end;
end;
-- 返回下次刷新时间，用于客户端判断是否成功执行
return refresh_time;
```

#### 合并为一个命令

主从模式下，复制单个命令的执行效率更高，因此采用此代码。

```lua
redis.replicate_commands();
local server_time = redis.call('time');
local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000));
local refresh_time = now + ARGV[1];
if (refresh_time > 0) then
    local array = {};
    local j = 0;
    for i = 2,#(ARGV) do
        j = j + 1;
        array[j] = refresh_time;
        j = j + 1;
        array[j] = ARGV[i];
    end;
    redis.call('zadd', KEYS[1], unpack(array));
end;
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
eval "redis.replicate_commands(); local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); local refresh_time = now + ARGV[1]; if (refresh_time > 0) then local array = {}; local j = 0; for i = 2,#(ARGV) do j = j + 1; array[j] = refresh_time; j = j + 1; array[j] = ARGV[i]; end; redis.call('zadd', KEYS[1], unpack(array)); end; return refresh_time;" 1 refresh 1000000 a1 b1 c1

# 3.判断 members 是否存在，分数是否大于当前时间
eval "local members = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'WITHSCORES', 'LIMIT', ARGV[3], ARGV[4]); local cnt = 0; local score = 0; local server_time = redis.call('time'); local now = math.floor((server_time[1] * 1000) + (server_time[2]/1000)); for i, value in ipairs(members) do local val = tostring(value); if ((val == 'a1') or (val == 'b1') or (val == 'c1')) then cnt = cnt + 1; else score = tonumber(value); if (score < now) then error('zadd failed, score:' .. score, 0); end; end; end; if (cnt < 3) then error('zadd failed, cnt:' .. cnt, 0); end; return {cnt, score};" 1 refresh 0 2734676370672 0 100
```



## REMOVE

### 程序逻辑

```lua
return redis.call('zrem', KEYS[1], unpack(ARGV));
```

### 测试代码

```lua
local members = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'LIMIT', ARGV[3], ARGV[4]);
local cnt = 0;
for i, value in ipairs(members) do
    local val = tostring(value);
    if ((val == 'a1') or (val == 'b1') or (val == 'c1')) then
        cnt = cnt + 1;
    end;
end;
if (cnt > 0) then
    error('zrem failed, cnt:' .. cnt, 0);
end;
return cnt;
```

### 测试步骤

```shell
# 1~3：先执行 PUT 测试步骤（添加相关数据并确认数据存在）

# 4.删除 member：a1 b1 c1
eval "return redis.call('zrem', KEYS[1], unpack(ARGV));" 1 refresh a1 b1 c1

# 5.测试删除是否成功
eval "local members = redis.call('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'LIMIT', ARGV[3], ARGV[4]); local cnt = 0; for i, value in ipairs(members) do local val = tostring(value); if ((val == 'a1') or (val == 'b1') or (val == 'c1')) then cnt = cnt + 1; end; end; if (cnt > 0) then error('zrem failed, cnt:' .. cnt, 0); end; return cnt;" 1 refresh 0 2734676370672 0 100
```
