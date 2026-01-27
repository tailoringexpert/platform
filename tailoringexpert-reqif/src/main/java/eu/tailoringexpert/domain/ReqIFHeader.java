package eu.tailoringexpert.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ReqIFHeader {

    @NonNull
    LocalDateTime creationTimestamp;

    @NonNull
    String identifier;

    String repositoryId;

    @NonNull
    String reqIFToolId;

    @NonNull
    String reqIFVersion;

    @NonNull
    String sourceToolId;

    @NonNull
    String title;


}
