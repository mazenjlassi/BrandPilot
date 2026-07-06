package com.example.metatry.Config;

import com.example.metatry.Enums.PlatformType;
import com.example.metatry.Enums.PostStatus;
import com.example.metatry.Enums.Role;
import com.example.metatry.Models.*;
import com.example.metatry.Repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampaignRepository campaignRepository;
    private final PostRepository postRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final ScrapedPostRepository scrapedPostRepository;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           CampaignRepository campaignRepository,
                           PostRepository postRepository,
                           CompanyProfileRepository companyProfileRepository,
                           ScrapedPostRepository scrapedPostRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.campaignRepository = campaignRepository;
        this.postRepository = postRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.scrapedPostRepository = scrapedPostRepository;
    }

    @Override
    public void run(String... args) {
        User admin = userRepository.findByName("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setName("admin");
            admin.setEmail("admin@metatry.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        if (userRepository.findByName("marketer").isEmpty()) {
            User marketer = new User();
            marketer.setName("marketer");
            marketer.setEmail("marketer@test.com");
            marketer.setPassword(passwordEncoder.encode("pass123"));
            marketer.setRole(Role.MARKETING);
            userRepository.save(marketer);
        }

        if (campaignRepository.count() == 0) {
            Campaign campaign = new Campaign();
            campaign.setName("Summer Launch 2025");
            campaign.setTopic("Product Launch");
            campaign.setCreatedAt(LocalDateTime.now());
            campaignRepository.save(campaign);

            Post post1 = new Post();
            post1.setTitle("Exciting News!");
            post1.setContent("We are thrilled to announce our new product launch this summer. Stay tuned for more updates!");
            post1.setHashtags("#Launch #Summer #Innovation");
            post1.setPlatform(PlatformType.INSTAGRAM);
            post1.setStatus(PostStatus.PUBLISHED);
            post1.setPublishedAt(LocalDateTime.now());
            post1.setGeneratedByAI(false);
            post1.setApproved(true);
            post1.setLikes(42);
            post1.setCommentsCount(7);
            post1.setShares(15);
            post1.setImpressions(1200);
            post1.setEngagementScore(5.3);
            post1.setCampaign(campaign);
            postRepository.save(post1);

            Post post2 = new Post();
            post2.setTitle("Behind the Scenes");
            post2.setContent("Here is a sneak peek at what we have been working on. Big things coming soon!");
            post2.setHashtags("#BehindTheScenes #ComingSoon #Tech");
            post2.setPlatform(PlatformType.LINKEDIN);
            post2.setStatus(PostStatus.DRAFT);
            post2.setScheduledAt(LocalDateTime.now().plusDays(7));
            post2.setGeneratedByAI(true);
            post2.setApproved(false);
            post2.setCampaign(campaign);
            postRepository.save(post2);

            Post post3 = new Post();
            post3.setTitle("Customer Spotlight");
            post3.setContent("We are proud to feature our customer success story. See how they transformed their business.");
            post3.setHashtags("#CustomerSuccess #Testimonial #Growth");
            post3.setPlatform(PlatformType.FACEBOOK);
            post3.setStatus(PostStatus.SCHEDULED);
            post3.setScheduledAt(LocalDateTime.now().plusDays(3));
            post3.setGeneratedByAI(false);
            post3.setApproved(true);
            post3.setCampaign(campaign);
            postRepository.save(post3);

            Campaign campaign2 = new Campaign();
            campaign2.setName("Brand Awareness Q3");
            campaign2.setTopic("Branding");
            campaign2.setCreatedAt(LocalDateTime.now());
            campaignRepository.save(campaign2);

            Post post4 = new Post();
            post4.setTitle("Our Mission");
            post4.setContent("Our mission is to empower businesses with cutting-edge technology solutions.");
            post4.setHashtags("#Mission #Vision #TechForGood");
            post4.setPlatform(PlatformType.LINKEDIN);
            post4.setStatus(PostStatus.PUBLISHED);
            post4.setPublishedAt(LocalDateTime.now());
            post4.setGeneratedByAI(false);
            post4.setApproved(true);
            post4.setLikes(89);
            post4.setCommentsCount(12);
            post4.setShares(34);
            post4.setImpressions(3400);
            post4.setEngagementScore(7.8);
            post4.setCampaign(campaign2);
            postRepository.save(post4);
        }

        if (companyProfileRepository.count() == 0) {
            CompanyProfile company = new CompanyProfile();
            company.setCompanyName("MetaTry");
            company.setInstagramUrl("https://www.instagram.com/metatry");
            company.setFacebookUrl("https://www.facebook.com/metatry");
            company.setLinkedinUrl("https://www.linkedin.com/company/metatry");
            companyProfileRepository.save(company);

            CompanyProfile company2 = new CompanyProfile();
            company2.setCompanyName("TechCorp");
            company2.setInstagramUrl("https://www.instagram.com/techcorp");
            company2.setFacebookUrl("https://www.facebook.com/techcorp");
            company2.setLinkedinUrl("https://www.linkedin.com/company/techcorp");
            companyProfileRepository.save(company2);

            ScrapedPost sp1 = new ScrapedPost();
            sp1.setCompanyName("MetaTry");
            sp1.setPlatform("LINKEDIN");
            sp1.setPostText("Excited to announce our latest partnership! This collaboration will drive innovation across the industry.");
            sp1.setPostUrl("https://linkedin.com/posts/metatry/1");
            sp1.setPostedAt("2025-05-15");
            sp1.setTopic("Partnership");
            sp1.setUsedForPattern(false);
            scrapedPostRepository.save(sp1);

            ScrapedPost sp2 = new ScrapedPost();
            sp2.setCompanyName("MetaTry");
            sp2.setPlatform("INSTAGRAM");
            sp2.setPostText("Behind the scenes at our team retreat! Building stronger connections and having fun.");
            sp2.setPostUrl("https://instagram.com/p/metatry/1");
            sp2.setPostedAt("2025-05-20");
            sp2.setTopic("Team Building");
            sp2.setUsedForPattern(true);
            scrapedPostRepository.save(sp2);

            ScrapedPost sp3 = new ScrapedPost();
            sp3.setCompanyName("TechCorp");
            sp3.setPlatform("FACEBOOK");
            sp3.setPostText("Our new product is live! Check out the features and get started today.");
            sp3.setPostUrl("https://facebook.com/techcorp/posts/1");
            sp3.setPostedAt("2025-05-10");
            sp3.setTopic("Product Launch");
            sp3.setUsedForPattern(true);
            scrapedPostRepository.save(sp3);
        }
    }
}
