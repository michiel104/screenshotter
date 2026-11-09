# screenshotter

Take screenshots of a list of websites, and puts them in a pdf with metadata. Useful for webpages with a page structure.

The tool starts a Firefox browser then defines a viewport and rectangle for which the screenshots are taken.

## Packaging

```
mvn clean package
java -jar target/screenshotter.jar
java -Doutput.directory=raw -jar target/screenshotter.jar

mkdir -p /pub/software/screenshotter/raw
cp target/screenshotter.jar /pub/software/screenshotter
cp screenshotter /usr/local/bin

screenshotter -t "dotJava home" -a "Michiel" dotJavaTest src/test/resources/website-pages-dotjava.txt -wx 3072 -wy 2000 -w 950 -h 1100 -x 1050 -y 570
```

## Run anywhere

If (after the mvn clean package) the target/screenshotter.jar was copied to the /pub/software/screenshotter folder, you can use the screenshotter shell script (place in /usr/local/bin) to run the command in any directory. If the jar file was copied elsewhere, please update the shell script.

To understand the parameters, see my website at [dotJava.nl](https://www.dotjava.nl/2026/11/08/screenshotter-to-convert-website-screenshots-into-a-pdf/) which is made with Wordpress and only uses a specific part of the screen with actual information. The parameters define what part should end up in the pdf file. The website list is included in the test resources.

When run from a folder, the (temporary) screenshot jpg files are placed in the raw folder (can be overridden with the java setting -Doutput.directory=), then when the pdf is generated it's placed in the current folder. All jpg files are removed when finished.
