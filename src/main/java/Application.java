import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class Application {

    public static void main(String[] args) {

        List<Integer> values = IntStream.rangeClosed(0, 20).boxed().collect(Collectors.toList());
        Executor executor = Executors.newFixedThreadPool(Math.min(values.size(), 100), r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        ExternalService externalService = new ExternalService(executor);
        long start = System.currentTimeMillis();
        List<CompletableFuture<Integer>> completableFutureList = values.stream().map(externalService::processAsync).collect(Collectors.toList());
        completableFutureList.stream().map(CompletableFuture::join).forEach(System.out::println);

        System.out.println("Took: " + (System.currentTimeMillis() - start) + " milliseconds");
    }
}

class ExternalService{

    public ExternalService(Executor executor) {
        this.executor = executor;
    }

    Executor executor;

    Integer process(Integer value){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value + 1 ;
    }

    CompletableFuture<Integer> processAsync(Integer value){
        return CompletableFuture.supplyAsync(()-> this.process(value), executor);
    }
}
