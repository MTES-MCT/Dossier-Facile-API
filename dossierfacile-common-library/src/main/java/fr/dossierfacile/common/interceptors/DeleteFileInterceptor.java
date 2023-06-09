package fr.dossierfacile.common.interceptors;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.StorageFile;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
@Slf4j
public class DeleteFileInterceptor extends EmptyInterceptor implements HibernatePropertiesCustomizer {

    @Autowired
    @Lazy
    private FileStorageService fileStorageService;

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put("hibernate.session_factory.interceptor", this);
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        try {
            if (entity instanceof StorageFile) {
                log.info("delete storage file in storage" + ((StorageFile) entity).getPath());
                fileStorageService.delete((StorageFile) entity);
            }
        } catch (Throwable e) {
            log.error("Unable to execute post delete operations! Sentry:" + Sentry.captureException(e), e);
        }
    }
}