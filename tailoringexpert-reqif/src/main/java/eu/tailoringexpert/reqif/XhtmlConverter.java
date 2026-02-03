package eu.tailoringexpert.reqif;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

public class XhtmlConverter implements BiFunction<String, Map<String, Object>, String> {

    @Override
    public String apply(String text, Map<String, Object> placeholders) {
        AtomicReference<String> updatedText = new AtomicReference<>(text);
        placeholders.entrySet()
            .forEach(entry -> updatedText.set(updatedText.get().replace(
                entry.getKey(),
                nonNull(entry.getValue()) ? entry.getValue().toString() : entry.getKey())
            ));
        Document document = Jsoup.parseBodyFragment(updatedText.get());
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        addPrefix(document);

        return document.getElementsByTag("reqif-xhtml:body").html();
    }

    private void addPrefix(Element element) {
        element.children().forEach(this::addPrefix);

        if (!(element instanceof Document)) {
            Element newElement = new Element("reqif-xhtml:" + element.tagName());
            newElement.attributes().addAll(element.attributes());

            for (Node node : element.childNodes()) {
                node.remove();
                newElement.appendChild(node);
            }

            element.replaceWith(newElement);
        }
    }

}
