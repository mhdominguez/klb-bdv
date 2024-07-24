package org.janelia.simview.klb.bdv;

import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.ImgLoaderIo;
import mpicbg.spim.data.generic.sequence.XmlIoBasicImgLoader;
import org.jdom2.Element;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static mpicbg.spim.data.XmlKeys.IMGLOADER_FORMAT_ATTRIBUTE_NAME;

@ImgLoaderIo(format = "klb", type = KlbImgLoader.class)
public class XmlIoKlbImageLoader implements XmlIoBasicImgLoader<KlbImgLoader> {

    @Override
    public Element toXml(final KlbImgLoader imgLoader, final File basePath) {
        final Element elem = new Element("ImageLoader");
        elem.setAttribute(IMGLOADER_FORMAT_ATTRIBUTE_NAME, "klb");
        elem.addContent(resolverToXml(imgLoader.getResolver(), basePath));
        return elem;
    }

    @Override
    public KlbImgLoader fromXml(final Element elem, final File basePath, final AbstractSequenceDescription<?, ?, ?> sequenceDescription) {
        final KlbPartitionResolver resolver = resolverFromXml(elem.getChild("Resolver"), basePath);
        return new KlbImgLoader(resolver, sequenceDescription);
    }

    private Element resolverToXml(final KlbPartitionResolver resolver, final File basePath) {
        final Element resolverElem = new Element("Resolver");
        final String type = resolver.getClass().getName();
        resolverElem.setAttribute("type", type);
        final List<KlbPartitionResolver.KlbViewSetupConfig> setups = resolver.getViewSetupConfigs();
        for (final KlbPartitionResolver.KlbViewSetupConfig setup : setups) {
            final Element templateElem = new Element("ViewSetupTemplate");
            String filePathTemplate = setup.getFilePathTemplate();
            // Convert absolute path to relative path
            Path absolutePath = Paths.get(filePathTemplate);
            Path relativePath = basePath.toPath().relativize(absolutePath);
            templateElem.addContent(XmlHelpers.textElement("template", relativePath.toString()));
            if (setup.getTimePoints() != null && !setup.getTimePoints().isEmpty()) {
                templateElem.addContent(XmlHelpers.textElement("timeTag", setup.getTimeTag()));
            }
            resolverElem.addContent(templateElem);
        }
        return resolverElem;
    }

    private KlbPartitionResolver resolverFromXml(final Element elem, final File basePath) {
        final String type = elem.getAttributeValue("type");
        if (type.equals(KlbPartitionResolver.class.getName()) || type.equals(KlbPartitionResolver.class.getName() + "Default")) {
            String timeTag = "TM";
            for (final Element e : elem.getChildren("MultiFileNameTag")) {
                final String dimension = XmlHelpers.getText(e, "dimension");
                if (dimension.equals("TIME")) {
                    timeTag = XmlHelpers.getText(e, "tag");
                    break;
                }
            }

            final KlbPartitionResolver resolver = new KlbPartitionResolver();
            for (final Element e : elem.getChildren("ViewSetupTemplate")) {
                String template = XmlHelpers.getText(e, "template");
                // Convert relative path to absolute path
                Path relativePath = basePath.toPath().resolve(template);
                template = relativePath.toAbsolutePath().toString();
                final String tag = XmlHelpers.getText(e, "timeTag");
                KlbPartitionResolver.KlbViewSetupConfig config = null;
                if (tag == null) {
                    if (timeTag != null) {
                        config = resolver.addViewSetup(template, timeTag);
                    } else {
                        config = resolver.addViewSetup(template);
                    }
                } else {
                    config = resolver.addViewSetup(template, tag);
                }
                if (config == null) {
                    throw new RuntimeException(String.format("Could not initialize ViewSetup %d because template file is missing: %s", resolver.getNumViewSetups(), template));
                }
            }
            return resolver;
        }

        throw new RuntimeException("Could not instantiate KlbPartitionResolver");
    }
}
