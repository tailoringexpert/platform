package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.SpecType;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.BiConsumer;

public class SpecTypesConsumer implements BiConsumer<Collection<SpecType>, ToXmlGenerator> {

    private static final QName QNAME_SPECTYPES = new QName("SPEC-TYPES");
    private BiConsumer<SpecType, ToXmlGenerator> specType = new SpecTypeConsumer();

    @Override
    public void accept(Collection<SpecType> specTypes, ToXmlGenerator generator) {
        generator.startWrappedValue(QNAME_SPECTYPES, QNAME_SPECTYPES);
        specTypes.forEach(value -> specType.accept(value, generator));
        generator.finishWrappedValue(QNAME_SPECTYPES, QNAME_SPECTYPES);
    }
}
