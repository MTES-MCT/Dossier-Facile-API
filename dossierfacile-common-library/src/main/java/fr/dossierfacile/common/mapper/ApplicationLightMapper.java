package fr.dossierfacile.common.mapper;

import fr.dossierfacile.common.entity.ApartmentSharing;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.model.apartment_sharing.ApplicationModel;
import fr.dossierfacile.common.model.apartment_sharing.DocumentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface ApplicationLightMapper {
    ApplicationModel toApplicationModel(ApartmentSharing apartmentSharing);

    @Mapping(target = "name", source="name", ignore = true)
    DocumentModel documentToDocumentModel(Document document);
}
