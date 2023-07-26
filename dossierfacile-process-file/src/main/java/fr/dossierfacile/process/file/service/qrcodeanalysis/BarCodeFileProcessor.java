package fr.dossierfacile.process.file.service.qrcodeanalysis;

import fr.dossierfacile.common.entity.BarCodeFileAnalysis;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.service.interfaces.FileStorageService;
import fr.dossierfacile.process.file.repository.BarCodeFileAnalysisRepository;
import fr.dossierfacile.process.file.util.InMemoryPdfFile;
import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class BarCodeFileProcessor {

    private static final String PDF_TYPE = "application/pdf";

    private final QrCodeFileAuthenticator qrCodeFileAuthenticator;
    private final TwoDDocFileAuthenticator twoDDocFileAuthenticator;

    private final BarCodeFileAnalysisRepository analysisRepository;
    private final FileStorageService fileStorageService;

    public void process(File file) {
        if (analysisRepository.hasNotAlreadyBeenAnalyzed(file) &&
                PDF_TYPE.equals(file.getStorageFile().getContentType())) {
            long start = System.currentTimeMillis();
            log.info("Starting analysis of file {}", file.getId());
            downloadAndAnalyze(file)
                    .ifPresent(analysis -> save(file, analysis));
            log.info("Analysis of file {} finished in {} ms", file.getId(), System.currentTimeMillis() - start);
        }
    }

    private Optional<BarCodeFileAnalysis> downloadAndAnalyze(File file) {
        try (InMemoryPdfFile inMemoryPdfFile = InMemoryPdfFile.create(file, fileStorageService)) {
            return analyze(inMemoryPdfFile)
                    .map(analysis -> {
                        boolean isAllowed = GuessedDocumentCategory.forFile(inMemoryPdfFile, analysis.getDocumentType())
                                .map(guess -> guess.isMatchingCategoryOf(file.getDocument()))
                                .orElse(true);
                        analysis.setAllowedInDocumentCategory(isAllowed);
                        return analysis;
                    });
        } catch (IOException e) {
            log.error("Unable to download file " + file.getStorageFile().getPath(), e);
            Sentry.captureMessage("Unable to download file " + file.getStorageFile().getPath());
        }
        return Optional.empty();
    }

    private Optional<BarCodeFileAnalysis> analyze(InMemoryPdfFile file) {
        if (file.hasQrCode()) {
            return qrCodeFileAuthenticator.analyze(file);
        }

        if (file.has2DDoc()) {
            return Optional.of(twoDDocFileAuthenticator.analyze(file.get2DDoc()));
        }

        return Optional.empty();
    }

    private void save(File file, BarCodeFileAnalysis analysis) {
        analysis.setFile(file);
        analysisRepository.save(analysis);
    }

}
