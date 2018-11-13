package android.net.apf;

import android.net.apf.ApfGenerator.IllegalInstructionException;
import android.net.apf.ApfGenerator.Register;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Bpf2Apf {
    private static int parseImm(String line, String arg) {
        if (arg.startsWith("#0x")) {
            long val_long = Long.parseLong(arg.substring(3), 16);
            if (val_long >= 0 && val_long <= Long.parseLong("ffffffff", 16)) {
                return new Long((val_long << 32) >> 32).intValue();
            }
            throw new IllegalArgumentException("Unhandled instruction: " + line);
        }
        throw new IllegalArgumentException("Unhandled instruction: " + line);
    }

    private static void convertLine(String line, ApfGenerator gen) throws IllegalInstructionException {
        if (line.indexOf("(") == 0 && line.indexOf(")") == 4 && line.indexOf(" ") == 5) {
            int label = Integer.parseInt(line.substring(1, 4));
            gen.defineLabel(Integer.toString(label));
            String opcode = line.substring(6, 10).trim();
            String arg = line.substring(15, Math.min(32, line.length())).trim();
            int memory_slot;
            if (opcode.equals("ld") || opcode.equals("ldh") || opcode.equals("ldb") || opcode.equals("ldx") || opcode.equals("ldxb") || opcode.equals("ldxh")) {
                Register dest = opcode.contains("x") ? Register.R1 : Register.R0;
                int offset;
                if (arg.equals("4*([14]&0xf)")) {
                    if (opcode.equals("ldxb")) {
                        gen.addLoadFromMemory(dest, 13);
                        return;
                    }
                    throw new IllegalArgumentException("Unhandled instruction: " + line);
                } else if (arg.equals("#pktlen")) {
                    if (opcode.equals("ld")) {
                        gen.addLoadFromMemory(dest, 14);
                        return;
                    }
                    throw new IllegalArgumentException("Unhandled instruction: " + line);
                } else if (arg.startsWith("#0x")) {
                    if (opcode.equals("ld")) {
                        gen.addLoadImmediate(dest, parseImm(line, arg));
                        return;
                    }
                    throw new IllegalArgumentException("Unhandled instruction: " + line);
                } else if (arg.startsWith("M[")) {
                    if (opcode.startsWith("ld")) {
                        memory_slot = Integer.parseInt(arg.substring(2, arg.length() - 1));
                        if (memory_slot < 0 || memory_slot >= 16 || (memory_slot >= 13 && memory_slot <= 15)) {
                            throw new IllegalArgumentException("Unhandled instruction: " + line);
                        }
                        gen.addLoadFromMemory(dest, memory_slot);
                        return;
                    }
                    throw new IllegalArgumentException("Unhandled instruction: " + line);
                } else if (arg.startsWith("[x + ")) {
                    offset = Integer.parseInt(arg.substring(5, arg.length() - 1));
                    if (opcode.equals("ld") || opcode.equals("ldx")) {
                        gen.addLoad32Indexed(dest, offset);
                        return;
                    } else if (opcode.equals("ldh") || opcode.equals("ldxh")) {
                        gen.addLoad16Indexed(dest, offset);
                        return;
                    } else if (opcode.equals("ldb") || opcode.equals("ldxb")) {
                        gen.addLoad8Indexed(dest, offset);
                        return;
                    } else {
                        return;
                    }
                } else {
                    offset = Integer.parseInt(arg.substring(1, arg.length() - 1));
                    if (opcode.equals("ld") || opcode.equals("ldx")) {
                        gen.addLoad32(dest, offset);
                        return;
                    } else if (opcode.equals("ldh") || opcode.equals("ldxh")) {
                        gen.addLoad16(dest, offset);
                        return;
                    } else if (opcode.equals("ldb") || opcode.equals("ldxb")) {
                        gen.addLoad8(dest, offset);
                        return;
                    } else {
                        return;
                    }
                }
            } else if (opcode.equals("st") || opcode.equals("stx")) {
                Register src = opcode.contains("x") ? Register.R1 : Register.R0;
                if (arg.startsWith("M[")) {
                    memory_slot = Integer.parseInt(arg.substring(2, arg.length() - 1));
                    if (memory_slot < 0 || memory_slot >= 16 || (memory_slot >= 13 && memory_slot <= 15)) {
                        throw new IllegalArgumentException("Unhandled instruction: " + line);
                    }
                    gen.addStoreToMemory(src, memory_slot);
                    return;
                }
                throw new IllegalArgumentException("Unhandled instruction: " + line);
            } else if (opcode.equals("add") || opcode.equals("and") || opcode.equals("or") || opcode.equals("sub")) {
                if (!arg.equals("x")) {
                    int imm = parseImm(line, arg);
                    if (opcode.equals("add")) {
                        gen.addAdd(imm);
                        return;
                    } else if (opcode.equals("and")) {
                        gen.addAnd(imm);
                        return;
                    } else if (opcode.equals("or")) {
                        gen.addOr(imm);
                        return;
                    } else if (opcode.equals("sub")) {
                        gen.addAdd(-imm);
                        return;
                    } else {
                        return;
                    }
                } else if (opcode.equals("add")) {
                    gen.addAddR1();
                    return;
                } else if (opcode.equals("and")) {
                    gen.addAndR1();
                    return;
                } else if (opcode.equals("or")) {
                    gen.addOrR1();
                    return;
                } else if (opcode.equals("sub")) {
                    gen.addNeg(Register.R1);
                    gen.addAddR1();
                    gen.addNeg(Register.R1);
                    return;
                } else {
                    return;
                }
            } else if (opcode.equals("jeq") || opcode.equals("jset") || opcode.equals("jgt") || opcode.equals("jge")) {
                boolean reg_compare;
                int val = 0;
                if (arg.startsWith("x")) {
                    reg_compare = true;
                } else {
                    reg_compare = false;
                    val = parseImm(line, arg);
                }
                int jt_offset = line.indexOf("jt");
                int jf_offset = line.indexOf("jf");
                String true_label = line.substring(jt_offset + 2, jf_offset).trim();
                String false_label = line.substring(jf_offset + 2).trim();
                boolean true_label_is_fallthrough = Integer.parseInt(true_label) == label + 1;
                boolean false_label_is_fallthrough = Integer.parseInt(false_label) == label + 1;
                if (!true_label_is_fallthrough || !false_label_is_fallthrough) {
                    if (opcode.equals("jeq")) {
                        if (!true_label_is_fallthrough) {
                            if (reg_compare) {
                                gen.addJumpIfR0EqualsR1(true_label);
                            } else {
                                gen.addJumpIfR0Equals(val, true_label);
                            }
                        }
                        if (!false_label_is_fallthrough) {
                            if (!true_label_is_fallthrough) {
                                gen.addJump(false_label);
                                return;
                            } else if (reg_compare) {
                                gen.addJumpIfR0NotEqualsR1(false_label);
                                return;
                            } else {
                                gen.addJumpIfR0NotEquals(val, false_label);
                                return;
                            }
                        }
                        return;
                    } else if (opcode.equals("jset")) {
                        if (reg_compare) {
                            gen.addJumpIfR0AnyBitsSetR1(true_label);
                        } else {
                            gen.addJumpIfR0AnyBitsSet(val, true_label);
                        }
                        if (!false_label_is_fallthrough) {
                            gen.addJump(false_label);
                            return;
                        }
                        return;
                    } else if (opcode.equals("jgt")) {
                        if (!true_label_is_fallthrough || (!false_label_is_fallthrough && reg_compare)) {
                            if (reg_compare) {
                                gen.addJumpIfR0GreaterThanR1(true_label);
                            } else {
                                gen.addJumpIfR0GreaterThan(val, true_label);
                            }
                        }
                        if (!false_label_is_fallthrough) {
                            if (!true_label_is_fallthrough || reg_compare) {
                                gen.addJump(false_label);
                                return;
                            } else {
                                gen.addJumpIfR0LessThan(val + 1, false_label);
                                return;
                            }
                        }
                        return;
                    } else if (opcode.equals("jge")) {
                        if (!false_label_is_fallthrough || (!true_label_is_fallthrough && reg_compare)) {
                            if (reg_compare) {
                                gen.addJumpIfR0LessThanR1(false_label);
                            } else {
                                gen.addJumpIfR0LessThan(val, false_label);
                            }
                        }
                        if (!true_label_is_fallthrough) {
                            if (!false_label_is_fallthrough || reg_compare) {
                                gen.addJump(true_label);
                                return;
                            } else {
                                gen.addJumpIfR0GreaterThan(val - 1, true_label);
                                return;
                            }
                        }
                        return;
                    } else {
                        return;
                    }
                }
                return;
            } else if (opcode.equals("ret")) {
                if (arg.equals("#0")) {
                    gen.addJump(ApfGenerator.DROP_LABEL);
                    return;
                } else {
                    gen.addJump(ApfGenerator.PASS_LABEL);
                    return;
                }
            } else if (opcode.equals("tax")) {
                gen.addMove(Register.R1);
                return;
            } else if (opcode.equals("txa")) {
                gen.addMove(Register.R0);
                return;
            } else {
                throw new IllegalArgumentException("Unhandled instruction: " + line);
            }
        }
        throw new IllegalArgumentException("Unhandled instruction: " + line);
    }

    public static byte[] convert(String bpf) throws IllegalInstructionException {
        ApfGenerator gen = new ApfGenerator();
        for (String line : bpf.split("\\n")) {
            convertLine(line, gen);
        }
        return gen.generate();
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder responseData = new StringBuilder();
        ApfGenerator gen = new ApfGenerator();
        while (true) {
            String line = in.readLine();
            if (line != null) {
                convertLine(line, gen);
            } else {
                System.out.write(gen.generate());
                return;
            }
        }
    }
}
