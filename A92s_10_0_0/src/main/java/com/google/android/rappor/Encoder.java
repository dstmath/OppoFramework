package com.google.android.rappor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.BitSet;
import java.util.Random;

public class Encoder {
    private static final byte HMAC_DRBG_TYPE_COHORT = 0;
    private static final byte HMAC_DRBG_TYPE_PRR = 1;
    public static final int MAX_BITS = 4096;
    public static final int MAX_BLOOM_HASHES = 8;
    public static final int MAX_COHORTS = 128;
    public static final int MIN_USER_SECRET_BYTES = 48;
    public static final long VERSION = 3;
    private final int cohort;
    private final byte[] encoderIdBytes;
    private final BitSet inputMask;
    private final MessageDigest md5;
    private final int numBits;
    private final int numBloomHashes;
    private final int numCohorts;
    private final double probabilityF;
    private final double probabilityP;
    private final double probabilityQ;
    private final Random random;
    private final MessageDigest sha256;
    private final byte[] userSecret;

    public Encoder(byte[] userSecret2, String encoderId, int numBits2, double probabilityF2, double probabilityP2, double probabilityQ2, int numCohorts2, int numBloomHashes2) {
        this(null, null, null, userSecret2, encoderId, numBits2, probabilityF2, probabilityP2, probabilityQ2, numCohorts2, numBloomHashes2);
    }

    public Encoder(Random random2, MessageDigest md52, MessageDigest sha2562, byte[] userSecret2, String encoderId, int numBits2, double probabilityF2, double probabilityP2, double probabilityQ2, int numCohorts2, int numBloomHashes2) {
        if (md52 != null) {
            this.md5 = md52;
        } else {
            try {
                this.md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException impossible) {
                throw new AssertionError(impossible);
            }
        }
        this.md5.reset();
        if (sha2562 != null) {
            this.sha256 = sha2562;
        } else {
            try {
                this.sha256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException impossible2) {
                throw new AssertionError(impossible2);
            }
        }
        this.sha256.reset();
        this.encoderIdBytes = encoderId.getBytes(StandardCharsets.UTF_8);
        if (random2 != null) {
            this.random = random2;
        } else {
            this.random = new SecureRandom();
        }
        checkArgument(userSecret2.length >= 48, "userSecret must be at least 48 bytes of high-quality entropy.");
        this.userSecret = userSecret2;
        checkArgument(probabilityF2 >= 0.0d && probabilityF2 <= 1.0d, "probabilityF must be on range [0.0, 1.0]");
        this.probabilityF = ((double) Math.round(probabilityF2 * 128.0d)) / 128.0d;
        checkArgument(probabilityP2 >= 0.0d && probabilityP2 <= 1.0d, "probabilityP must be on range [0.0, 1.0]");
        this.probabilityP = probabilityP2;
        checkArgument(probabilityQ2 >= 0.0d && probabilityQ2 <= 1.0d, "probabilityQ must be on range [0.0, 1.0]");
        this.probabilityQ = probabilityQ2;
        checkArgument(numBits2 >= 1 && numBits2 <= 4096, "numBits must be on range [1, 4096].");
        this.numBits = numBits2;
        this.inputMask = new BitSet(numBits2);
        this.inputMask.set(0, numBits2, true);
        checkArgument(numBloomHashes2 >= 1 && numBloomHashes2 <= numBits2, "numBloomHashes must be on range [1, numBits).");
        this.numBloomHashes = numBloomHashes2;
        checkArgument(numCohorts2 >= 1 && numCohorts2 <= 128, "numCohorts must be on range [1, 128].");
        checkArgument(((numCohorts2 + -1) & numCohorts2) == 0, "numCohorts must be a power of 2.");
        this.numCohorts = numCohorts2;
        this.cohort = (numCohorts2 - 1) & (Math.abs(ByteBuffer.wrap(new HmacDrbg(userSecret2, new byte[]{HMAC_DRBG_TYPE_COHORT}).nextBytes(4)).getInt()) % 128);
    }

    public double getProbabilityF() {
        return this.probabilityF;
    }

    public double getProbabilityP() {
        return this.probabilityP;
    }

    public double getProbabilityQ() {
        return this.probabilityQ;
    }

    public int getNumBits() {
        return this.numBits;
    }

    public int getNumBloomHashes() {
        return this.numBloomHashes;
    }

    public int getNumCohorts() {
        return this.numCohorts;
    }

    public int getCohort() {
        return this.cohort;
    }

    public String getEncoderId() {
        return new String(this.encoderIdBytes, StandardCharsets.UTF_8);
    }

    public byte[] encodeBoolean(boolean bool) {
        BitSet input = new BitSet(this.numBits);
        input.set(0, bool);
        return encodeBits(input);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.util.BitSet.set(int, boolean):void}
     arg types: [int, int]
     candidates:
      ClspMth{java.util.BitSet.set(int, int):void}
      ClspMth{java.util.BitSet.set(int, boolean):void} */
    public byte[] encodeOrdinal(int ordinal) {
        checkArgument(ordinal >= 0 && ordinal < this.numBits, "Ordinal value must be in range [0, numBits).");
        BitSet input = new BitSet(this.numBits);
        input.set(ordinal, true);
        return encodeBits(input);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.util.BitSet.set(int, boolean):void}
     arg types: [int, int]
     candidates:
      ClspMth{java.util.BitSet.set(int, int):void}
      ClspMth{java.util.BitSet.set(int, boolean):void} */
    public byte[] encodeString(String string) {
        byte[] digest;
        byte[] stringInUtf8 = string.getBytes(StandardCharsets.UTF_8);
        byte[] message = ByteBuffer.allocate(stringInUtf8.length + 4).putInt(this.cohort).put(stringInUtf8).array();
        synchronized (this) {
            this.md5.reset();
            digest = this.md5.digest(message);
        }
        boolean z = false;
        verify(digest.length == 16);
        if (this.numBloomHashes <= digest.length / 2) {
            z = true;
        }
        verify(z);
        BitSet input = new BitSet(this.numBits);
        for (int i = 0; i < this.numBloomHashes; i++) {
            input.set((((digest[i * 2] & 255) * HMAC_DRBG_TYPE_COHORT) + (digest[(i * 2) + 1] & 255)) % this.numBits, true);
        }
        return encodeBits(input);
    }

    public byte[] encodeBits(byte[] bits) {
        return encodeBits(BitSet.valueOf(bits));
    }

    private byte[] encodeBits(BitSet bits) {
        byte[] encodedBytes = computeInstantaneousRandomizedResponse(computePermanentRandomizedResponse(bits)).toByteArray();
        byte[] output = new byte[((this.numBits + 7) / 8)];
        verify(encodedBytes.length <= output.length);
        System.arraycopy(encodedBytes, 0, output, 0, encodedBytes.length);
        return output;
    }

    private BitSet computePermanentRandomizedResponse(BitSet bits) {
        byte[] personalizationString;
        BitSet masked = new BitSet();
        masked.or(bits);
        masked.andNot(this.inputMask);
        checkArgument(masked.isEmpty(), "Input bits had bits set past Encoder's numBits limit.");
        if (this.probabilityF == 0.0d) {
            return bits;
        }
        synchronized (this) {
            personalizationString = new byte[Math.min(20, this.sha256.getDigestLength() + 1)];
            personalizationString[0] = HMAC_DRBG_TYPE_PRR;
            this.sha256.reset();
            this.sha256.update(this.encoderIdBytes);
            this.sha256.update(new byte[]{HMAC_DRBG_TYPE_COHORT});
            this.sha256.update(bits.toByteArray());
            System.arraycopy(this.sha256.digest(personalizationString), 0, personalizationString, 1, personalizationString.length - 1);
        }
        byte[] pseudorandomStream = new HmacDrbg(this.userSecret, personalizationString).nextBytes(this.numBits);
        verify(this.numBits <= pseudorandomStream.length);
        int probabilityFTimes128 = (int) Math.round(this.probabilityF * 128.0d);
        BitSet result = new BitSet(this.numBits);
        for (int i = 0; i < this.numBits; i++) {
            int pseudorandomByte = pseudorandomStream[i] & 255;
            if ((pseudorandomByte >> 1) < probabilityFTimes128) {
                result.set(i, (pseudorandomByte & 1) > 0);
            } else {
                result.set(i, bits.get(i));
            }
        }
        return result;
    }

    private BitSet computeInstantaneousRandomizedResponse(BitSet bits) {
        BitSet masked = new BitSet();
        masked.or(bits);
        masked.andNot(this.inputMask);
        checkArgument(masked.isEmpty(), "Input bits had bits set past Encoder's numBits limit.");
        if (this.probabilityP == 0.0d && this.probabilityQ == 1.0d) {
            return bits;
        }
        BitSet response = new BitSet(this.numBits);
        for (int i = 0; i < this.numBits; i++) {
            response.set(i, ((double) this.random.nextFloat()) < (bits.get(i) ? this.probabilityQ : this.probabilityP));
        }
        return response;
    }

    private static void checkArgument(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    private static void verify(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }
}
