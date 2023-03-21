package club.keleya.crawl;

import club.keleya.data.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CheckAlive {
    /**
     * 异步检测存活
     * @param data 单个data数据
     * @return Boolean
     */
    private static CompletableFuture<Boolean> checkAlive(Data data) {
        return CompletableFuture.supplyAsync(() -> {
            String host = data.getHost();
            int port = data.getPort();

            try {
                InetAddress address = InetAddress.getByName(host);
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(address, port), 5000);
                socket.close();
                data.setIsAlive(true);
//                System.out.println("alive: " + address);
            } catch (IOException e) {
                data.setIsAlive(false);
            }
            return true;
        });
    }

    /**
     * 启动异步检测的入口方法
     * @param dataSet 包含Data对象的HashSet集合
     */
    public static void  check(HashSet<Data> dataSet) {
        try {
            CompletableFuture.allOf(dataSet.stream()
                    .map(CheckAlive::checkAlive).toArray(CompletableFuture[]::new)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
