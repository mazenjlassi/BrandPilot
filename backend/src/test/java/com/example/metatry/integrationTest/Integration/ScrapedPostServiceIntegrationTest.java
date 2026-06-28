package com.example.metatry.integrationTest.Integration;

import com.example.metatry.Models.ScrapedPost;
import com.example.metatry.Repositories.ScrapedPostRepository;
import com.example.metatry.Services.ScrapedPostService;
import com.example.metatry.integrationTest.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ScrapedPostServiceIntegrationTest {

    @Autowired
    private ScrapedPostService scrapedPostService;

    @Autowired
    private ScrapedPostRepository scrapedPostRepository;

    @BeforeEach
    void setUp() {
        scrapedPostRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        scrapedPostRepository.deleteAll();
    }

    private ScrapedPost createPost(String company, String platform, String text, String url) {
        return scrapedPostRepository.save(ScrapedPost.builder()
                .companyName(company).platform(platform)
                .postText(text).postUrl(url)
                .usedForPattern(false).build());
    }

    @Test
    void getAll_returnsAll() {
        createPost("C1", "linkedin", "Text1", "http://url1");
        createPost("C1", "instagram", "Text2", "http://url2");

        List<ScrapedPost> all = scrapedPostService.getAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void getById_found() {
        ScrapedPost saved = createPost("C1", "linkedin", "Text", "http://url");

        ScrapedPost found = scrapedPostService.getById(saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getCompanyName()).isEqualTo("C1");
    }

    @Test
    void getById_notFound() {
        ScrapedPost found = scrapedPostService.getById(99999L);
        assertThat(found).isNull();
    }

    @Test
    void getByCompanyName_returnsFiltered() {
        createPost("C1", "linkedin", "T1", "http://u1");
        createPost("C2", "linkedin", "T2", "http://u2");

        List<ScrapedPost> result = scrapedPostService.getByCompanyName("C1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyName()).isEqualTo("C1");
    }

    @Test
    void getByPlatform_returnsFiltered() {
        createPost("C1", "linkedin", "T1", "http://u1");
        createPost("C1", "facebook", "T2", "http://u2");

        List<ScrapedPost> result = scrapedPostService.getByPlatform("linkedin");
        assertThat(result).hasSize(1);
    }

    @Test
    void getByTopic_returnsFiltered() {
        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("C1").platform("linkedin")
                .postText("Text").topic("AI").build());
        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("C1").platform("facebook")
                .postText("Text2").topic("Security").build());

        List<ScrapedPost> result = scrapedPostService.getByTopic("AI");
        assertThat(result).hasSize(1);
    }

    @Test
    void save_createsNew() {
        ScrapedPost post = ScrapedPost.builder()
                .companyName("C1").platform("linkedin")
                .postText("New post").build();

        ScrapedPost saved = scrapedPostService.save(post);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void save_duplicateByUrl_returnsExisting() {
        createPost("C1", "linkedin", "Text", "http://linkedin.com/posts/1");

        ScrapedPost dup = ScrapedPost.builder()
                .companyName("C1").platform("linkedin")
                .postText("Text").postUrl("http://linkedin.com/posts/1")
                .build();

        ScrapedPost result = scrapedPostService.save(dup);
        List<ScrapedPost> all = scrapedPostService.getAll();
        assertThat(all).hasSize(1);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void save_duplicateByText_returnsExisting() {
        createPost("C1", "linkedin", "Duplicate text", "http://url1");

        ScrapedPost dup = ScrapedPost.builder()
                .companyName("C1").platform("linkedin")
                .postText("Duplicate text").postUrl("http://url2")
                .build();

        ScrapedPost result = scrapedPostService.save(dup);
        List<ScrapedPost> all = scrapedPostService.getAll();
        assertThat(all).hasSize(1);
    }

    @Test
    void delete_removesPost() {
        ScrapedPost saved = createPost("C1", "linkedin", "Text", "http://url");

        scrapedPostService.delete(saved.getId());

        assertThat(scrapedPostService.getById(saved.getId())).isNull();
    }

    @Test
    void countByCompany_returnsCorrectCount() {
        createPost("C1", "linkedin", "T1", "http://u1");
        createPost("C1", "facebook", "T2", "http://u2");
        createPost("C2", "linkedin", "T3", "http://u3");

        long count = scrapedPostService.countByCompany("C1");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByPlatform_returnsCorrectCount() {
        createPost("C1", "linkedin", "T1", "http://u1");
        createPost("C2", "linkedin", "T2", "http://u2");
        createPost("C1", "facebook", "T3", "http://u3");

        long count = scrapedPostService.countByPlatform("linkedin");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void getUnusedForPattern_returnsOnlyUnused() {
        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("C1").platform("linkedin")
                .postText("Used").usedForPattern(true).build());
        scrapedPostRepository.save(ScrapedPost.builder()
                .companyName("C1").platform("linkedin")
                .postText("Unused").usedForPattern(false).build());

        List<ScrapedPost> unused = scrapedPostService.getUnusedForPattern();
        assertThat(unused).hasSize(1);
        assertThat(unused.get(0).getPostText()).isEqualTo("Unused");
    }

    @Test
    void markAsUsedForPattern_updatesFlag() {
        ScrapedPost saved = createPost("C1", "linkedin", "Text", "http://url");

        scrapedPostService.markAsUsedForPattern(saved.getId());

        ScrapedPost refreshed = scrapedPostService.getById(saved.getId());
        assertThat(refreshed.getUsedForPattern()).isTrue();
    }

    @Test
    void getDistinctCompanies_returnsUnique() {
        createPost("C1", "linkedin", "T1", "http://u1");
        createPost("C2", "linkedin", "T2", "http://u2");
        createPost("C1", "facebook", "T3", "http://u3");

        List<String> companies = scrapedPostService.getDistinctCompanies();
        assertThat(companies).containsExactlyInAnyOrder("C1", "C2");
    }
}
