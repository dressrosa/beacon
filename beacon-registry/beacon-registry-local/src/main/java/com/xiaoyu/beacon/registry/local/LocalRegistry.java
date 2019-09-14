/**
 * 唯有读书,不慵不扰
 * 
 */
package com.xiaoyu.beacon.registry.local;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xiaoyu.beacon.common.bean.BeaconPath;
import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.utils.StringUtil;
import com.xiaoyu.beacon.registry.AbstractRegistry;

/**
 * 本地注册
 * 
 * @author hongyu
 * @date 2019-08
 * @description 用于本地简化测试使用
 */
public class LocalRegistry extends AbstractRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRegistry.class);

    // private static final String ROOT = "/beacon";

    private static final String PROVIDERS = "/providers";

    private static final String CONSUMERS = "/consumers";

    private static final String Local_Registry = "beacon-local-registry";

    // 有一个空格
    private static final String Sepa = ", ";

    private static final String Parent = System.getProperty("user.home") + File.separator + ".beacon" + File.separator;

    /**
     * 格式: /beacon/service-name/consumers/service-detail-info
     */
    @Override
    public void registerService(BeaconPath beaconPath) {
        Path filePath = Paths.get(Parent, Local_Registry);
        String detailInfo = beaconPath.toPath();
        List<String> lines = null;
        try {
            lines = Files.readAllLines(filePath);
        } catch (Exception e) {
            LOG.error("" + e);
        }
        /// com.a.helloservice/providers/xxxxxx,xxxxx,xxxxx
        /// com.a.helloservice/consumers/xxxxxx,xxxxx,xxxxx
        String s = null;
        if (beaconPath.getSide() == From.CLIENT) {
            s = "consumers";
        } else {
            s = "providers";
        }
        String service = beaconPath.getService();
        if (lines != null && !lines.isEmpty()) {
            int size = lines.size();
            for (int i = size - 1; i >= 0; i--) {
                String[] arr = lines.get(i).split("/");
                if (!service.equals(arr[0])) {
                    continue;
                }
                // detailinfos
                if (StringUtil.isNotBlank(arr[2]) && s.equals(arr[1])) {
                    String[] parr = arr[2].split(Sepa);
                    for (int k = 0; k < parr.length; k++) {
                        BeaconPath b = BeaconPath.toEntity(parr[k]);
                        if (b.getSide() == From.CLIENT) {
                            if (b.getHost().equals(beaconPath.getHost())
                                    && b.getGroup().equals(beaconPath.getGroup())) {
                                // 已经存在的给去掉,后面重新加
                                parr[k] = "";
                            }
                        } else {
                            if (b.getHost().equals(beaconPath.getHost())
                                    && b.getGroup().equals(beaconPath.getGroup())
                                    && b.getPort().equals(beaconPath.getPort())) {
                                parr[k] = "";
                            }
                        }
                    }
                    for (int k = 0; k < parr.length; k++) {
                        if ("".equals(parr[k])) {
                            continue;
                        }
                        detailInfo += Sepa + parr[k];
                    }
                }
                break;
            }
        }
        String bPath = null;
        String side = "";
        if (beaconPath.getSide() == From.CLIENT) {
            side = "consumer";
            bPath = consumerPath(service);
        } else {
            side = "provider";
            bPath = providerPath(service);
        }
        FileChannel ch = null;
        try {
            ch = FileChannel.open(filePath, StandardOpenOption.APPEND);
            ch.write(ByteBuffer.wrap(this.fullPath(bPath, detailInfo).getBytes()));
            // 保存本地
            this.storeLocalService(service, beaconPath);
            LOG.info("Register " + side + " service to local->{}", (bPath + "/" + beaconPath.toPath()));
        } catch (IOException e1) {
            LOG.error("Register service->{} error->{}", service, e1);
        } finally {
            if (ch != null) {
                try {
                    ch.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void unregisterService(BeaconPath beaconPath) {

        String detailInfo = "";
        List<String> lines = this.getFileLines();
        /// com.a.helloservice/providers/xxxxxx,xxxxx,xxxxx
        /// com.a.helloservice/consumers/xxxxxx,xxxxx,xxxxx
        String s = null;
        if (beaconPath.getSide() == From.CLIENT) {
            s = "consumers";
        } else {
            s = "providers";
        }
        String service = beaconPath.getService();
        if (lines != null && !lines.isEmpty()) {
            int size = lines.size();
            for (int i = size - 1; i >= 0; i--) {
                String[] arr = lines.get(i).split("/");
                if (!service.equals(arr[0])) {
                    continue;
                }
                // detailinfos
                if (StringUtil.isNotBlank(arr[2]) && s.equals(arr[1])) {
                    String[] parr = arr[2].split(Sepa);
                    for (int k = 0; k < parr.length; k++) {
                        BeaconPath b = BeaconPath.toEntity(parr[k]);
                        if (b.getSide() == From.CLIENT) {
                            if (b.getHost().equals(beaconPath.getHost())
                                    && b.getGroup().equals(beaconPath.getGroup())) {
                                parr[k] = "";
                            }
                        } else {
                            if (b.getHost().equals(beaconPath.getHost())
                                    && b.getGroup().equals(beaconPath.getGroup())
                                    && b.getPort().equals(beaconPath.getPort())) {
                                parr[k] = "";
                            }
                        }
                    }
                    for (int k = 0; k < parr.length; k++) {
                        if (StringUtil.isBlank(parr[k])) {
                            continue;
                        }
                        detailInfo += parr[k];
                        if (k != parr.length - 1) {
                            detailInfo += Sepa;
                        }
                    }
                }
                break;
            }
        }
        String bPath = null;
        if (beaconPath.getSide() == From.CLIENT) {
            bPath = consumerPath(service);
        } else {
            bPath = providerPath(service);
        }
        Path filePath = Paths.get(Parent, Local_Registry);
        FileChannel ch = null;
        try {
            ch = FileChannel.open(filePath, StandardOpenOption.APPEND);
            ch.write(ByteBuffer.wrap(this.fullPath(bPath, detailInfo).getBytes()));
            super.removeLocalService(service, beaconPath);
            LOG.info("Unregister service in local->{}", this.fullPath(bPath, detailInfo));
        } catch (IOException e1) {
            LOG.error("Unregister service->{} error->{}", service, e1);
        } finally {
            if (ch != null) {
                try {
                    ch.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void address(String addr) {
        try {
            File f = new File(Parent);
            if (!f.exists()) {
                f.mkdir();
            }
            FileAttribute<?> attrs = PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rw-------"));
            Path path = Paths.get(Parent, Local_Registry);
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                Files.createFile(path, attrs);
            }
        } catch (IOException e) {
            LOG.error("" + e);
        }
    }

    @Override
    public boolean isInit() {
        Path filePath = Paths.get(Parent, Local_Registry);
        return Files.exists(filePath, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public void close() {
        Path path = Paths.get(Parent, Local_Registry);
        try {
            long lines = Files.lines(path).count();
            // 超过500行则删除
            if (lines > 500) {
                Files.delete(path);
            }
        } catch (IOException e) {
        }
        return;
    }

    private final String providerPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(service).append(PROVIDERS);
        return builder.toString();
    }

    private final String fullPath(String path, String detailInfo) {
        StringBuilder builder = new StringBuilder();
        builder.append(path).append("/").append(detailInfo).append("\n");
        return builder.toString();
    }

    private final String consumerPath(String service) {
        StringBuilder builder = new StringBuilder();
        builder.append(service).append(CONSUMERS);
        return builder.toString();
    }

    @Override
    public boolean doDiscoverService(String service) {
        List<String> lines = this.getFileLines();
        /// com.a.helloservice/providers/xxxxxx,xxxxx,xxxxx
        /// com.a.helloservice/consumers/xxxxxx,xxxxx,xxxxx
        int size = lines.size();
        for (int i = size - 1; i >= 0; i--) {
            String[] arr = lines.get(i).split("/");
            if (service.equals(arr[0]) && "providers".equals(arr[1])) {
                if (StringUtil.isNotBlank(arr[2])) {
                    return true;
                }
            }
        }
        return false;
    }

    private final List<String> getFileLines() {
        Path filePath = Paths.get(Parent, Local_Registry);
        List<String> lines = new ArrayList<>(0);
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            LOG.error("" + e);
        }
        return lines;
    }

    /**
     * client启动时,本地并没有对应的已存在的provider,这里初始化所属的provider
     */
    @Override
    public void doInitProviders(String service) {
        List<String> lines = getFileLines();
        int size = lines.size();
        /// com.a.helloservice/providers/xxxxxx,xxxxx,xxxxx
        /// com.a.helloservice/consumers/xxxxxx,xxxxx,xxxxx
        for (int i = size - 1; i >= 0; i--) {
            String[] arr = lines.get(i).split("/");
            if (service.equals(arr[0]) && "providers".equals(arr[1])) {
                if (StringUtil.isNotBlank(arr[2])) {
                    String[] parr = arr[2].split(Sepa);
                    for (String p : parr) {
                        super.storeLocalService(service, BeaconPath.toEntity(p));
                    }
                }
                break;
            }
        }
    }

}
