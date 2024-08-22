package com.igeeksky.redis.stream;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/21
 */
public class GroupCreateOptions {

    private final boolean mkstream;

    private final Long entriesRead;

    private GroupCreateOptions(boolean mkstream, Long entriesRead) {
        this.mkstream = mkstream;
        this.entriesRead = entriesRead;
    }

    public boolean valid() {
        return mkstream || entriesRead != null;
    }

    public boolean isMkstream() {
        return mkstream;
    }

    public Long getEntriesRead() {
        return entriesRead;
    }

    public static GroupCreateOptions mkstream() {
        return new GroupCreateOptions(true, null);
    }

    public static GroupCreateOptions entriesRead(Long entriesRead) {
        return new GroupCreateOptions(false, entriesRead);
    }

    public static GroupCreateOptions newArgs(boolean mkstream, Long entriesRead) {
        return new GroupCreateOptions(mkstream, entriesRead);
    }

}