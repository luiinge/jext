package jext.plugin;

import java.io.IOException;
import java.nio.file.*;
import org.slf4j.*;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

class DropinProcessor {


    private static final Logger LOGGER = LoggerFactory.getLogger(DropinProcessor.class);

    private final Path dropinsFolder;
    private final PluginWharehouse wharehouse;

    public DropinProcessor(Path dropinsFolder, PluginWharehouse wharehouse) {
        this.dropinsFolder = dropinsFolder;
        this.wharehouse = wharehouse;
    }


    public void processDropinsFolder() throws IOException {
        try (var walker = Files.walk(dropinsFolder, 0)) {
            walker
            .filter(file -> file.endsWith(".zip"))
            .forEach(this::processDropin);
        }
    }



    private void processDropin(Path zipFile) {
        try {
            LOGGER.info("Processing drop-in file {} ...", zipFile);
            Path temporaryFolder = Files.createTempDirectory("jext-dropin");
            extractZip(zipFile, temporaryFolder);

            var temporaryWharehouse = PluginWharehouse.builder()
                .atPath(temporaryFolder)
                .usingLayout(wharehouse.layoutType())
                .build();
            if (!temporaryWharehouse.isEmpty()) {
                temporaryWharehouse.copyContentsTo(wharehouse);
            }
        } catch (IOException e) {
            LOGGER.error("Error processing drop-in file {} : {}", zipFile, e.getMessage());
            LOGGER.debug("",e);
        }
    }


    private void extractZip(Path zipFile, Path temporaryFolder) throws ZipException {
        new ZipFile(zipFile.toFile()).extractAll(temporaryFolder.toString());
    }


}
