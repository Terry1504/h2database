/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.store;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.h2.value.ValueLob;

/**
 * This factory creates in-memory objects and temporary files. It is used on the
 * client side.
 */
public class LobStorageFrontend implements LobStorageInterface {

    /**
     * The table id for session variables (LOBs not assigned to a table).
     */
    public static final int TABLE_ID_SESSION_VARIABLE = -1;

    /**
     * The table id for temporary objects (not assigned to any object).
     */
    public static final int TABLE_TEMP = -2;

    /**
     * The table id for result sets.
     */
    public static final int TABLE_RESULT = -3;

    private final DataHandler handler;

    public LobStorageFrontend(DataHandler handler) {
        this.handler = handler;
    }

    @Override
    public void removeLob(ValueLob lob) {
        // not stored in the database
    }

    /**
     * Get the input stream for the given lob.
     *
     * @param lob the lob
     * @param hmac the message authentication code (for remote input streams)
     * @param byteCount the number of bytes to read, or -1 if not known
     * @return the stream
     */
    @Override
    public InputStream getInputStream(ValueLob lob, byte[] hmac,
            long byteCount) throws IOException {
        if (byteCount < 0) {
            byteCount = Long.MAX_VALUE;
        }
        return new BufferedInputStream(new LobStorageRemoteInputStream(
                handler, lob, hmac, byteCount));
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public ValueLob copyLob(ValueLob old, int tableId, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllForTable(int tableId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueLob createBlob(InputStream in, long maxLength) {
        // need to use a temp file, because the input stream could come from
        // the same database, which would create a weird situation (trying
        // to read a block while writing something)
        return ValueLob.createTempBlob(in, maxLength, handler);
    }

    /**
     * Create a CLOB object.
     *
     * @param reader the reader
     * @param maxLength the maximum length (-1 if not known)
     * @return the LOB
     */
    @Override
    public ValueLob createClob(Reader reader, long maxLength) {
        // need to use a temp file, because the input stream could come from
        // the same database, which would create a weird situation (trying
        // to read a block while writing something)
        return ValueLob.createTempClob(reader, maxLength, handler);
    }

    @Override
    public void init() {
        // nothing to do
    }

}
