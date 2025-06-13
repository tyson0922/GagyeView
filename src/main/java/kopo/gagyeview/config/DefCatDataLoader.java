package kopo.gagyeview.config;

import kopo.gagyeview.dto.DefCatDTO;
import kopo.gagyeview.persistence.mapper.ICatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefCatDataLoader implements CommandLineRunner {

    private final ICatMapper catMapper;

    @Override
    public void run(String... args) throws Exception {
        if (catMapper.countDefCat() == 0) {
            String systemId = "system";

            List<DefCatDTO> defaultCategories = List.of(
                    new DefCatDTO(null, "식비", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "교통비", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "주거비", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "통신비", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "교육비", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "쇼핑/의류", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "건강/의료", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "문화생활", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "기타지출", "지출", systemId, null, systemId, null),
                    new DefCatDTO(null, "월급", "수입", systemId, null, systemId, null),
                    new DefCatDTO(null, "용돈", "수입", systemId, null, systemId, null),
                    new DefCatDTO(null, "이자소득", "수입", systemId, null, systemId, null),
                    new DefCatDTO(null, "투자수익", "수입", systemId, null, systemId, null),
                    new DefCatDTO(null, "기타수입", "수입", systemId, null, systemId, null)
            );

            for (DefCatDTO dto : defaultCategories) {
                catMapper.insertDefCat(dto);
            }

            log.info("✅ DEF_CAT table initialized with {} entries.", defaultCategories.size());
        } else {
            log.info("📌 DEF_CAT already populated. Skipping initialization.");
        }
    }
}
