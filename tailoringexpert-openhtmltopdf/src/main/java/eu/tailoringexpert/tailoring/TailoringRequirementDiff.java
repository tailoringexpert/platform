package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TailoringRequirementDiff {
    TailoringRequirement base;
    TailoringRequirement other;
}
