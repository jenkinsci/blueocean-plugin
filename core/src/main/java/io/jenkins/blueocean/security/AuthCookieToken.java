package io.jenkins.blueocean.security;


import com.google.common.primitives.Longs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import javax.servlet.http.Cookie;

public class AuthCookieToken {

    private static final int VERSION = 1;

    // Date the cookie expires
    public final long expires;
    // The user id representing the owner of this cookie
    public final String user;


    public AuthCookieToken(long expires, String user) {
        this.expires = expires;
        this.user = user;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeInt(VERSION);
        os.writeLong(expires);
        os.writeUTF(user);
        os.flush();

        return bos.toByteArray();
    }

    public static AuthCookieToken decode(byte[] bytes) throws IOException {
        ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytes));
        int version = is.readInt();
        switch (version) {
            case 1:
                long expires = is.readLong();
                String user = is.readUTF();

                return new AuthCookieToken(expires, user);
            default:
                throw new AssertionError();
        }

    }
}
