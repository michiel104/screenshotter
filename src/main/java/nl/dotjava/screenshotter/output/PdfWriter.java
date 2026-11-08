package nl.dotjava.screenshotter.output;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** combines jpg files of a directory into one pdf, one image per page, scaled to fit */
public class PdfWriter {

    private final PDRectangle pageSize;
    private PDDocument pdDocument;
    private String title;
    private String author;
    private String creator;
    private String keywords;

    public PdfWriter(boolean createLandscape) {
        this.pageSize = createLandscape
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;
    }

    public void setMetadata(String title, String author, String creator, String keywords) {
        this.title = title;
        this.author = author;
        this.creator = creator;
        this.keywords = keywords;
    }

    public Path write(Path imageDirectory, Path pdfFile) throws IOException {
        List<Path> images = listJpgs(imageDirectory);
        if (images.isEmpty()) {
            throw new IOException("No jpg files found in " + imageDirectory);
        }
        pdDocument = new PDDocument();
        for (Path image : images) {
            addPage(image);
        }
        addMetadata();
        updateXmp();
        pdDocument.save(pdfFile.toFile());
        return pdfFile;
    }

    private void addPage(Path image) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(image.toString(), pdDocument);
        PDPage page = new PDPage(pageSize);
        pdDocument.addPage(page);

        float scale = Math.min(pageSize.getWidth() / pdImage.getWidth(), pageSize.getHeight() / pdImage.getHeight());
        float w = pdImage.getWidth() * scale;
        float h = pdImage.getHeight() * scale;
        try (PDPageContentStream content = new PDPageContentStream(pdDocument, page)) {
            content.drawImage(pdImage, (pageSize.getWidth() - w) / 2, (pageSize.getHeight() - h) / 2, w, h);
        }
    }

    private static List<Path> listJpgs(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(f -> f.getFileName().toString().toLowerCase().endsWith(".jpg"))
                    .sorted(Comparator.comparing((Path f) -> f.getFileName().toString()))
                    .toList();
        }
    }

    private void addMetadata() throws IOException {
        PDDocumentInformation info = pdDocument.getDocumentInformation();
        if (title != null) info.setTitle(title);
        if (author != null) info.setAuthor(author);
        if (creator != null) info.setCreator(creator);
        if (keywords != null) info.setKeywords(keywords);
        pdDocument.setDocumentInformation(info);
    }

    private void updateXmp() throws IOException {
        PDMetadata metadata = pdDocument.getDocumentCatalog().getMetadata();
        if (metadata == null) {
            return;
        }
        try {
            XMPMetadata xmp;
            try (InputStream in = metadata.exportXMPMetadata()) {
                xmp = new DomXmpParser().parse(in);
            }

            if (title != null || author != null) {
                DublinCoreSchema dc = xmp.getDublinCoreSchema();
                if (dc == null) {
                    dc = xmp.createAndAddDublinCoreSchema();
                    if (title != null) {
                        if (dc.getTitleProperty() != null) {
                            dc.removeProperty(dc.getTitleProperty());
                        }
                        dc.setTitle(title);
                    }
                    if (author != null) {
                        if (dc.getCreatorsProperty() != null) {
                            dc.removeProperty(dc.getCreatorsProperty());
                        }
                        dc.addCreator(author);
                    }
                }
            }

            if (creator != null) {
                XMPBasicSchema basic = xmp.getXMPBasicSchema();
                if (basic == null) {
                    basic = xmp.createAndAddXMPBasicSchema();
                    basic.setCreatorTool(creator);
                }
            }
            if (keywords != null) {
                AdobePDFSchema pdf = xmp.getAdobePDFSchema();
                if (pdf == null) pdf = xmp.createAndAddAdobePDFSchema();
                pdf.setKeywords(keywords);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new XmpSerializer().serialize(xmp, out, true);
            metadata.importXMPMetadata(out.toByteArray());
        } catch (XmpParsingException | TransformerException e) {
            System.err.println("Warning: XMP metadata could not be updated (" + e.getMessage() + "), only the Info dictionary was written");
        }
    }
}
