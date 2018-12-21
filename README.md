# Unmixing Helper

## How to compile
```sh
mvn
```

## How to try
1. Launch ImageJ by the following command.
    ```sh
    mvn -Pexec -Dmain-class=jp.ac.keio.bio.fun.imagej.unmixinghelper.UnmixingHelperDialog
    ```
2. Open several image files from ImageJ
3. Launch this plugin from [Plugin] -> [Unmixing Helper]

## How to try (batch mode)
1. Prepare 6 image files and place it in this directory.
2. Modify following lines in `src/main/java/jp/ac/keio/bio/fun/imagej/unmixinghelper/UnmixingHelper.java`
    ```java
    ArrayList<File> fl = new ArrayList<>();
        final File file1 = new File("CFP_30ms.tif");
        final File file2 = new File("GFP_50ms.tif");
        final File file3 = new File("Orange_30ms.tif");
        final File file4 = new File("Cherry_30ms.tif");
        final File file5 = new File("Keima_10ms.tif");
        final File file6 = new File("YFP_50ms.tif");
    ```
    to fit with your image files (just change the filenames in the code).
3. Launch ImageJ and execute the plugin by the following command.
    ```sh
    mvn -Pexec -Dmain-class=jp.ac.keio.bio.fun.imagej.unmixinghelper.UnmixingHelper
    ```

