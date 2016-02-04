package io.jenkins.blueocean.security;

import org.apache.commons.codec.binary.Base64;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.salt.StringFixedSaltGenerator;

public class CryptService {
    public final Crypter cookies;

    public CryptService(String cookiePassword, String cookieSalt) {
        this.cookies = new Crypter(cookiePassword, cookieSalt);
    }

    /**
     * For use in cookies, tokens, etc rather than hashing passwords
     */
    public static final class Crypter {
        private final StandardPBEByteEncryptor crypter = new StandardPBEByteEncryptor();

        public Crypter(String password, String salt) {
            this.crypter.setPassword(password);
            this.crypter.setSaltGenerator(new StringFixedSaltGenerator(salt));
        }

        public byte[] crypt(byte[] bytes) {
            return crypter.encrypt(bytes);
        }

        public byte[] decrypt(byte[] bytes) {
            return crypter.decrypt(bytes);
        }

        public static void main(String[] args) throws Exception {
            RandomSaltGenerator generator = new RandomSaltGenerator();
            byte[] bytes = generator.generateSalt(64);
            System.out.println(Base64.encodeBase64String(bytes));
        }
    }
}
