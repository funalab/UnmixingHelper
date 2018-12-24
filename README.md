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
2. Modify following lines in [`src/main/java/jp/ac/keio/bio/fun/imagej/unmixinghelper/UnmixingHelper.java`](https://github.com/funalab/UnmixingHelper/blob/1d79d5828bccbedab4f95bd836b778dfc3bbb728/src/main/java/jp/ac/keio/bio/fun/imagej/unmixinghelper/UnmixingHelper.java#L164-L171)
    ```java
    String[] testfile = {
        "06GRCOC_CFP_5ms.tif",
        "06GRCOC_GFP_5ms.tif",
        "06GRCOC_Orange_5ms.tif",
        "06GRCOC_Cherry_5ms.tif",
        "06GRCOC_Keima_2.5ms.tif",
        "06GRCOC_YFP_5ms.tif",
    };
    ```
    to fit with your image files (just change the filenames in the code).
3. Launch ImageJ and execute the plugin by the following command.
    ```sh
    mvn -Pexec -Dmain-class=jp.ac.keio.bio.fun.imagej.unmixinghelper.UnmixingHelper
    ```

