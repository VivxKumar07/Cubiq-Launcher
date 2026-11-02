package git.artdeell.mojo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MojoApplication {
    public static final ExecutorService sExecutorService = Executors.newFixedThreadPool(4);
}
