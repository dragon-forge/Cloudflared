package org.zeith.cloudflared.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

import org.zeith.cloudflared.forge1710.CloudflaredForge;

public class OSArch {

    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_ARCH = System.getProperty("os.arch");

    private static final ArchDistro ARCHITECTURE;

    private static final InstructionSet INSTRUCTIONS;

    static {
        String osn = OS_NAME.toLowerCase(Locale.ROOT);

        ArchDistro distro = ArchDistro.UNKNOWN;
        InstructionSet instructionSet = InstructionSet.X86;

        boolean is64 = OS_ARCH.contains("64");

        if (OS_ARCH.equals("amd64") || (OS_ARCH.contains("x86") && is64)) {
            instructionSet = InstructionSet.X86_64;
        }
        String details = "Unknown";
        if (osn.contains("windows")) {

            distro = is64 ? ArchDistro.WINDOWS_X64 : ArchDistro.WINDOWS_X86;
        } else if (osn.contains("mac os")) {

            ProcessBuilder pb = new ProcessBuilder("sysctl", "-n", "machdep.cpu.brand_string");

            try {
                Process p = pb.start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

                    details = br.readLine();
                    p.waitFor();
                }
            } catch (InterruptedException | java.io.IOException x) {
                CloudflaredForge.LOG.error("Failed to get processor details for MacOS", x);
            }

            if (details.toLowerCase(Locale.ROOT)
                .contains("apple")) {

                distro = ArchDistro.MACOS_APPLE;
                instructionSet = is64 ? InstructionSet.ARM_64 : InstructionSet.ARM_32;
            } else {

                distro = ArchDistro.MACOS_INTEL;
                instructionSet = is64 ? InstructionSet.X86_64 : InstructionSet.X86;
            }
        } else if (isUnix()) {

            distro = ArchDistro.GNU_LINUX;
            instructionSet = OS_ARCH.contains("arm") ? (is64 ? InstructionSet.ARM_64 : InstructionSet.ARM_32)
                : (is64 ? InstructionSet.X86_64 : InstructionSet.X86);
        }

        INSTRUCTIONS = instructionSet;
        ARCHITECTURE = distro;
    }

    public static ArchDistro getArchitecture() {
        return ARCHITECTURE;
    }

    public static InstructionSet getInstructions() {
        return INSTRUCTIONS;
    }

    public static boolean isUnix() {
        return (OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix"));
    }

    public enum InstructionSet {
        X86,
        X86_64,
        ARM_32,
        ARM_64
    }

    public enum ArchDistro {

        WINDOWS_X86(OSType.WINDOWS),
        WINDOWS_X64(OSType.WINDOWS),
        GNU_LINUX(OSType.UNIX),
        MACOS_INTEL(OSType.MACOS),
        MACOS_APPLE(OSType.MACOS),
        UNKNOWN(OSType.UNKNOWN);

        private final OSArch.OSType type;

        ArchDistro(OSArch.OSType type) {
            this.type = type;
        }

        public OSArch.OSType getType() {
            return this.type;
        }
    }

    public enum OSType {
        WINDOWS,
        MACOS,
        UNIX,
        UNKNOWN
    }
}
