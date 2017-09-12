package it.hackcaffebabe.jdrive;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.Firmware;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PowerSource;
import oshi.hardware.Sensors;
import oshi.hardware.UsbDevice;
import oshi.software.os.FileSystem;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.ProcessSort;
import oshi.util.FormatUtil;
import oshi.util.Util;

public class SystemInfo
{
    private static Logger log = LogManager.getLogger(SystemInfo.class.getName());

    public static void doLog() {
        oshi.SystemInfo si = new oshi.SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        log.info("OS:");
        log.info(os);

        printComputerSystem(hal.getComputerSystem());

        printProcessor(hal.getProcessor());

        printMemory(hal.getMemory());

        printCpu(hal.getProcessor());

        printProcesses(os, hal.getMemory());

        printSensors(hal.getSensors());

        printPowerSources(hal.getPowerSources());

        printDisks(hal.getDiskStores());

        printFileSystem(os.getFileSystem());

        printNetworkInterfaces(hal.getNetworkIFs());

        printNetworkParameters(os.getNetworkParams());

        printDisplays(hal.getDisplays());

        printUsbDevices(hal.getUsbDevices(true));
    }

    private static void printComputerSystem(final ComputerSystem computerSystem) {
        log.info("\nSystem:");
        log.info("manufacturer: " + computerSystem.getManufacturer());
        log.info("model: " + computerSystem.getModel());
        log.info("serialnumber: " + computerSystem.getSerialNumber());
        final Firmware firmware = computerSystem.getFirmware();
        log.info("firmware:");
        log.info("  manufacturer: " + firmware.getManufacturer());
        log.info("  name: " + firmware.getName());
        log.info("  description: " + firmware.getDescription());
        log.info("  version: " + firmware.getVersion());
        log.info("  release date: " + (firmware.getReleaseDate() == null ? "unknown"
                : firmware.getReleaseDate() == null ? "unknown" : FormatUtil.formatDate(firmware.getReleaseDate())));
        final Baseboard baseboard = computerSystem.getBaseboard();
        log.info("baseboard:");
        log.info("  manufacturer: " + baseboard.getManufacturer());
        log.info("  model: " + baseboard.getModel());
        log.info("  version: " + baseboard.getVersion());
        log.info("  serialnumber: " + baseboard.getSerialNumber());
    }

    private static void printProcessor(CentralProcessor processor) {
        log.info("\nProcessor:");
        log.info(processor);
        log.info(" " + processor.getPhysicalProcessorCount() + " physical CPU(s)");
        log.info(" " + processor.getLogicalProcessorCount() + " logical CPU(s)");

        log.info("Identifier: " + processor.getIdentifier());
        log.info("ProcessorID: " + processor.getProcessorID());
    }

    private static void printMemory(GlobalMemory memory) {
        log.info("\nMemory:");
        log.info("Memory: " + FormatUtil.formatBytes(memory.getAvailable()) + "/"
                + FormatUtil.formatBytes(memory.getTotal()));
        log.info("Swap used: " + FormatUtil.formatBytes(memory.getSwapUsed()) + "/"
                + FormatUtil.formatBytes(memory.getSwapTotal()));
    }

    private static void printCpu(CentralProcessor processor) {
        log.info("\nCPU stats:");
        log.info("Uptime: " + FormatUtil.formatElapsedSecs(processor.getSystemUptime()));

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        log.info("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks));
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        log.info("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks));
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

        log.info(String.format(
                "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%",
                100d * user / totalCpu, 100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu,
                100d * iowait / totalCpu, 100d * irq / totalCpu, 100d * softirq / totalCpu, 100d * steal / totalCpu));
        log.info(String.format("CPU load: %.1f%% (counting ticks)", processor.getSystemCpuLoadBetweenTicks() * 100));
        log.info(String.format("CPU load: %.1f%% (OS MXBean)", processor.getSystemCpuLoad() * 100));
        double[] loadAverage = processor.getSystemLoadAverage(3);
        log.info("CPU load averages:" + (loadAverage[0] < 0 ? " N/A" : String.format(" %.2f", loadAverage[0]))
                + (loadAverage[1] < 0 ? " N/A" : String.format(" %.2f", loadAverage[1]))
                + (loadAverage[2] < 0 ? " N/A" : String.format(" %.2f", loadAverage[2])));
        // per core CPU
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks();
        for (double avg : load) {
            procCpu.append(String.format(" %.1f%%", avg * 100));
        }
        log.info(procCpu.toString());
    }

    private static void printProcesses(OperatingSystem os, GlobalMemory memory) {
        log.info("\nProcesses:");
        log.info("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount());
        // Sort by highest CPU
        List<OSProcess> procs = Arrays.asList(os.getProcesses(5, ProcessSort.CPU));

        log.info("   PID  %CPU %MEM       VSZ       RSS Name");
        for (int i = 0; i < procs.size() && i < 5; i++) {
            OSProcess p = procs.get(i);
            log.info(String.format(" %5d %5.1f %4.1f %9s %9s %s", p.getProcessID(),
                    100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                    100d * p.getResidentSetSize() / memory.getTotal(), FormatUtil.formatBytes(p.getVirtualSize()),
                    FormatUtil.formatBytes(p.getResidentSetSize()), p.getName()));
        }
    }

    private static void printSensors(Sensors sensors) {
        log.info("\nSensors:");
        log.info(String.format(" CPU Temperature: %.1f°C", sensors.getCpuTemperature()));
        log.info(" Fan Speeds: " + Arrays.toString(sensors.getFanSpeeds()));
        log.info(String.format(" CPU Voltage: %.1fV", sensors.getCpuVoltage()));
    }

    private static void printPowerSources(PowerSource[] powerSources) {
        log.info("\nPower:");
        StringBuilder sb = new StringBuilder("Power: ");
        if (powerSources.length == 0) {
            sb.append("Unknown");
        } else {
            double timeRemaining = powerSources[0].getTimeRemaining();
            if (timeRemaining < -1d) {
                sb.append("Charging");
            } else if (timeRemaining < 0d) {
                sb.append("Calculating time remaining");
            } else {
                sb.append(String.format("%d:%02d remaining", (int) (timeRemaining / 3600),
                        (int) (timeRemaining / 60) % 60));
            }
        }
        for (PowerSource pSource : powerSources) {
            sb.append(String.format("%n %s @ %.1f%%", pSource.getName(), pSource.getRemainingCapacity() * 100d));
        }
        log.info(sb.toString());
    }

    private static void printDisks(HWDiskStore[] diskStores) {
        log.info("\nDisks:");
        for (HWDiskStore disk : diskStores) {
            boolean readwrite = disk.getReads() > 0 || disk.getWrites() > 0;
            log.info(String.format(" %s: (model: %s - S/N: %s) size: %s, reads: %s (%s), writes: %s (%s), xfer: %s ms",
                    disk.getName(), disk.getModel(), disk.getSerial(),
                    disk.getSize() > 0 ? FormatUtil.formatBytesDecimal(disk.getSize()) : "?",
                    readwrite ? disk.getReads() : "?", readwrite ? FormatUtil.formatBytes(disk.getReadBytes()) : "?",
                    readwrite ? disk.getWrites() : "?", readwrite ? FormatUtil.formatBytes(disk.getWriteBytes()) : "?",
                    readwrite ? disk.getTransferTime() : "?"));
            HWPartition[] partitions = disk.getPartitions();
            if (partitions == null) {
                continue;
            }
            for (HWPartition part : partitions) {
                log.info(String.format(" |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%s", part.getIdentification(),
                        part.getName(), part.getType(), part.getMajor(), part.getMinor(),
                        FormatUtil.formatBytesDecimal(part.getSize()),
                        part.getMountPoint().isEmpty() ? "" : " @ " + part.getMountPoint()));
            }
        }
    }

    private static void printFileSystem(FileSystem fileSystem) {
        log.info("\nFile System:");
        log.info(String.format(" File Descriptors: %d/%d", fileSystem.getOpenFileDescriptors(),
                fileSystem.getMaxFileDescriptors()));

        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            log.info(String.format(" %s (%s) [%s] %s of %s free (%.1f%%) is %s " +
                            (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s") +
                            " and is mounted at %s", fs.getName(),
                    fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), fs.getType(),
                    FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable / total,
                    fs.getVolume(), fs.getLogicalVolume(), fs.getMount()));
        }
    }

    private static void printNetworkInterfaces(NetworkIF[] networkIFs) {
        log.info("\nNetwork interfaces:");
        for (NetworkIF net : networkIFs) {
            log.info(String.format(" Name: %s (%s)", net.getName(), net.getDisplayName()));
            log.info(String.format("   MAC Address: %s", net.getMacaddr()));
            log.info(String.format("   MTU: %s, Speed: %s", net.getMTU(), FormatUtil.formatValue(net.getSpeed(), "bps")));
            log.info(String.format("   IPv4: %s", Arrays.toString(net.getIPv4addr())));
            log.info(String.format("   IPv6: %s", Arrays.toString(net.getIPv6addr())));
            boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0
                    || net.getPacketsSent() > 0;
            log.info(String.format("   Traffic: received %s/%s%s; transmitted %s/%s%s",
                    hasData ? net.getPacketsRecv() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
                    hasData ? " (" + net.getInErrors() + " err)" : "",
                    hasData ? net.getPacketsSent() + " packets" : "?",
                    hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?",
                    hasData ? " (" + net.getOutErrors() + " err)" : ""));
        }
    }

    private static void printNetworkParameters(NetworkParams networkParams) {
        log.info("\nNetwork parameters:");
        log.info(String.format(" Host name: %s", networkParams.getHostName()));
        log.info(String.format(" Domain name: %s", networkParams.getDomainName()));
        log.info(String.format(" DNS servers: %s", Arrays.toString(networkParams.getDnsServers())));
        log.info(String.format(" IPv4 Gateway: %s", networkParams.getIpv4DefaultGateway()));
        log.info(String.format(" IPv6 Gateway: %s", networkParams.getIpv6DefaultGateway()));
    }

    private static void printDisplays(Display[] displays) {
        log.info("\nDisplays:");
        int i = 0;
        for (Display display : displays) {
            log.info(" Display " + i + ":");
            log.info(display.toString());
            i++;
        }
    }

    private static void printUsbDevices(UsbDevice[] usbDevices) {
        log.info("\nUSB Devices:");
        for (UsbDevice usbDevice : usbDevices) {
            log.info(usbDevice.toString());
        }
    }
}
