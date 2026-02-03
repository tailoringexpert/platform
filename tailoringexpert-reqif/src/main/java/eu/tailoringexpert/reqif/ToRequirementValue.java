package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.TailoringRequirement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.nonNull;

public class ToRequirementValue implements Function<TailoringRequirement, AttributeValueString> {

    private static final AttributeDefinitionString definition = AttributeDefinitionString.builder()
        .identifier("a-description")
        .longName("Description")
        .type(DatatypeDefinitionString.builder()
            .maxLength(100000)
            .identifier("dt-string")
            .longName("String")
            .build())
        .build();


    private BiFunction<String, Map<String, Object>, String> toXHTML = (text, placeholders) -> {
        AtomicReference<String> updatedText = new AtomicReference<>(text);
        placeholders.entrySet()
            .forEach(entry -> updatedText.set(updatedText.get().replace(
                entry.getKey(),
                nonNull(entry.getValue()) ? entry.getValue().toString() : entry.getKey())
            ));
        Document document = Jsoup.parseBodyFragment(updatedText.get());
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        return document.body().html();

    };

    @Override
    public AttributeValueString apply(TailoringRequirement value) {
        return AttributeValueString.builder()
            .definition(definition)
            .theValue(toXHTML.apply(value.getText(), Map.of()))
            .build();
    }


    private void addPrefix(Element element) {
        element.children().forEach(this::addPrefix);

        // Ersetze das aktuelle Element, wenn es nicht der Root-Node (Document) ist
        if (!(element instanceof Document)) {
            Element newElement = new Element("reqif-xhtml:" + element.tagName());
            newElement.attributes().addAll(element.attributes());

            for (Node node : element.childNodes()) {
                node.remove(); // Entfernen aus altem Element
                newElement.appendChild(node); // Anhängen an neues Element
            }

            // Altes Element durch neues ersetzen
            element.replaceWith(newElement);
        }
    }

}






