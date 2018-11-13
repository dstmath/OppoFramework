package com.android.dex;

import android.icu.lang.UProperty;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public final class TableOfContents {
    public final Section annotationSetRefLists = new Section(4098);
    public final Section annotationSets = new Section(4099);
    public final Section annotations = new Section(8196);
    public final Section annotationsDirectories = new Section(8198);
    public int apiLevel;
    public int checksum;
    public final Section classDatas = new Section(8192);
    public final Section classDefs = new Section(6);
    public final Section codes = new Section(UProperty.MASK_LIMIT);
    public int dataOff;
    public int dataSize;
    public final Section debugInfos = new Section(8195);
    public final Section encodedArrays = new Section(8197);
    public final Section fieldIds = new Section(4);
    public int fileSize;
    public final Section header = new Section(0);
    public int linkOff;
    public int linkSize;
    public final Section mapList = new Section(4096);
    public final Section methodIds = new Section(5);
    public final Section protoIds = new Section(3);
    public final Section[] sections = new Section[]{this.header, this.stringIds, this.typeIds, this.protoIds, this.fieldIds, this.methodIds, this.classDefs, this.mapList, this.typeLists, this.annotationSetRefLists, this.annotationSets, this.classDatas, this.codes, this.stringDatas, this.debugInfos, this.annotations, this.encodedArrays, this.annotationsDirectories};
    public byte[] signature = new byte[20];
    public final Section stringDatas = new Section(8194);
    public final Section stringIds = new Section(1);
    public final Section typeIds = new Section(2);
    public final Section typeLists = new Section(4097);

    public static class Section implements Comparable<Section> {
        public int byteCount = 0;
        public int off = -1;
        public int size = 0;
        public final short type;

        public Section(int type) {
            this.type = (short) type;
        }

        public boolean exists() {
            return this.size > 0;
        }

        public int compareTo(Section section) {
            if (this.off == section.off) {
                return 0;
            }
            return this.off < section.off ? -1 : 1;
        }

        public String toString() {
            return String.format("Section[type=%#x,off=%#x,size=%#x]", new Object[]{Short.valueOf(this.type), Integer.valueOf(this.off), Integer.valueOf(this.size)});
        }
    }

    public void readFrom(Dex dex) throws IOException {
        readHeader(dex.open(0));
        readMap(dex.open(this.mapList.off));
        computeSizesFromOffsets();
    }

    private void readHeader(com.android.dex.Dex.Section headerIn) throws UnsupportedEncodingException {
        byte[] magic = headerIn.readByteArray(8);
        if (DexFormat.isSupportedDexMagic(magic)) {
            this.apiLevel = DexFormat.magicToApi(magic);
            this.checksum = headerIn.readInt();
            this.signature = headerIn.readByteArray(20);
            this.fileSize = headerIn.readInt();
            int headerSize = headerIn.readInt();
            if (headerSize != 112) {
                throw new DexException("Unexpected header: 0x" + Integer.toHexString(headerSize));
            }
            int endianTag = headerIn.readInt();
            if (endianTag != DexFormat.ENDIAN_TAG) {
                throw new DexException("Unexpected endian tag: 0x" + Integer.toHexString(endianTag));
            }
            this.linkSize = headerIn.readInt();
            this.linkOff = headerIn.readInt();
            this.mapList.off = headerIn.readInt();
            if (this.mapList.off == 0) {
                throw new DexException("Cannot merge dex files that do not contain a map");
            }
            this.stringIds.size = headerIn.readInt();
            this.stringIds.off = headerIn.readInt();
            this.typeIds.size = headerIn.readInt();
            this.typeIds.off = headerIn.readInt();
            this.protoIds.size = headerIn.readInt();
            this.protoIds.off = headerIn.readInt();
            this.fieldIds.size = headerIn.readInt();
            this.fieldIds.off = headerIn.readInt();
            this.methodIds.size = headerIn.readInt();
            this.methodIds.off = headerIn.readInt();
            this.classDefs.size = headerIn.readInt();
            this.classDefs.off = headerIn.readInt();
            this.dataSize = headerIn.readInt();
            this.dataOff = headerIn.readInt();
            return;
        }
        throw new DexException("Unexpected magic: " + Arrays.toString(magic));
    }

    private void readMap(com.android.dex.Dex.Section in) throws IOException {
        int mapSize = in.readInt();
        Section previous = null;
        int i = 0;
        while (i < mapSize) {
            short type = in.readShort();
            in.readShort();
            Section section = getSection(type);
            int size = in.readInt();
            int offset = in.readInt();
            if ((section.size == 0 || section.size == size) && (section.off == -1 || section.off == offset)) {
                section.size = size;
                section.off = offset;
                if (previous == null || previous.off <= section.off) {
                    previous = section;
                    i++;
                } else {
                    throw new DexException("Map is unsorted at " + previous + ", " + section);
                }
            }
            throw new DexException("Unexpected map value for 0x" + Integer.toHexString(type));
        }
        Arrays.sort(this.sections);
    }

    public void computeSizesFromOffsets() {
        int end = this.dataOff + this.dataSize;
        for (int i = this.sections.length - 1; i >= 0; i--) {
            Section section = this.sections[i];
            if (section.off != -1) {
                if (section.off > end) {
                    throw new DexException("Map is unsorted at " + section);
                }
                section.byteCount = end - section.off;
                end = section.off;
            }
        }
    }

    private Section getSection(short type) {
        for (Section section : this.sections) {
            if (section.type == type) {
                return section;
            }
        }
        throw new IllegalArgumentException("No such map item: " + type);
    }

    public void writeHeader(com.android.dex.Dex.Section out, int api) throws IOException {
        out.write(DexFormat.apiToMagic(api).getBytes("UTF-8"));
        out.writeInt(this.checksum);
        out.write(this.signature);
        out.writeInt(this.fileSize);
        out.writeInt(112);
        out.writeInt(DexFormat.ENDIAN_TAG);
        out.writeInt(this.linkSize);
        out.writeInt(this.linkOff);
        out.writeInt(this.mapList.off);
        out.writeInt(this.stringIds.size);
        out.writeInt(this.stringIds.off);
        out.writeInt(this.typeIds.size);
        out.writeInt(this.typeIds.off);
        out.writeInt(this.protoIds.size);
        out.writeInt(this.protoIds.off);
        out.writeInt(this.fieldIds.size);
        out.writeInt(this.fieldIds.off);
        out.writeInt(this.methodIds.size);
        out.writeInt(this.methodIds.off);
        out.writeInt(this.classDefs.size);
        out.writeInt(this.classDefs.off);
        out.writeInt(this.dataSize);
        out.writeInt(this.dataOff);
    }

    public void writeMap(com.android.dex.Dex.Section out) throws IOException {
        int count = 0;
        for (Section section : this.sections) {
            if (section.exists()) {
                count++;
            }
        }
        out.writeInt(count);
        for (Section section2 : this.sections) {
            if (section2.exists()) {
                out.writeShort(section2.type);
                out.writeShort((short) 0);
                out.writeInt(section2.size);
                out.writeInt(section2.off);
            }
        }
    }
}
