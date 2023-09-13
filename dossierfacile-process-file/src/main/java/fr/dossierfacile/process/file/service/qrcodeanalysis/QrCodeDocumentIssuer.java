package fr.dossierfacile.process.file.service.qrcodeanalysis;

import fr.dossierfacile.process.file.util.InMemoryPdfFile;

import java.util.Optional;

public abstract class QrCodeDocumentIssuer<REQUEST extends AuthenticationRequest> {

    protected abstract Optional<REQUEST> buildAuthenticationRequestFor(InMemoryPdfFile pdfFile);

    protected abstract AuthenticationResult authenticate(InMemoryPdfFile pdfFile, REQUEST authenticationRequest);

    public Optional<AuthenticationResult> tryToAuthenticate(InMemoryPdfFile pdfFile) {
        return buildAuthenticationRequestFor(pdfFile).map(request -> authenticate(pdfFile, request));
    }

}