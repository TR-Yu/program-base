package tech.tryu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * banner print
 *
 * @author tryu
 */
public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        String banner =
                "     ██╗ █████╗ ██╗   ██╗ █████╗     ██████╗  █████╗ ███████╗███████╗\n" +
                "     ██║██╔══██╗██║   ██║██╔══██╗    ██╔══██╗██╔══██╗██╔════╝██╔════╝\n" +
                "     ██║███████║██║   ██║███████║    ██████╔╝███████║███████╗█████╗  \n" +
                "██   ██║██╔══██║╚██╗ ██╔╝██╔══██║    ██╔══██╗██╔══██║╚════██║██╔══╝  \n" +
                "╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║    ██████╔╝██║  ██║███████║███████╗\n" +
                " ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚══════╝╚══════╝\n";

        logger.info("Starting {}...\n{}", App.class.getSimpleName(), banner);
    }


}