package com.example.metatry.Models;

import com.example.metatry.Enums.ImageSize;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostImageTest {

    @Test
    void builder_setsAllFields() {
        Post post = Post.builder().id(1L).build();
        PostImage image = PostImage.builder()
                .id(1L)
                .imageUrl("https://res.cloudinary.com/img.jpg")
                .imagePrompt("A beautiful sunset")
                .size(ImageSize.LANDSCAPE)
                .selected(true)
                .post(post)
                .build();

        assertThat(image.getId()).isEqualTo(1L);
        assertThat(image.getImageUrl()).isEqualTo("https://res.cloudinary.com/img.jpg");
        assertThat(image.getImagePrompt()).isEqualTo("A beautiful sunset");
        assertThat(image.getSize()).isEqualTo(ImageSize.LANDSCAPE);
        assertThat(image.getSelected()).isTrue();
        assertThat(image.getPost()).isSameAs(post);
    }

    @Test
    void noArgsConstructor_selectedDefaultsToTrue() {
        PostImage image = new PostImage();
        assertThat(image.getSelected()).isTrue();
        assertThat(image.getId()).isNull();
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        Post post = Post.builder().id(2L).build();
        PostImage image = new PostImage(1L, "url", "prompt", ImageSize.SQUARE, false, post);

        assertThat(image.getId()).isEqualTo(1L);
        assertThat(image.getImageUrl()).isEqualTo("url");
        assertThat(image.getImagePrompt()).isEqualTo("prompt");
        assertThat(image.getSize()).isEqualTo(ImageSize.SQUARE);
        assertThat(image.getSelected()).isFalse();
        assertThat(image.getPost()).isSameAs(post);
    }

    @Test
    void setters_updateFields() {
        PostImage image = new PostImage();

        image.setImageUrl("https://new-url.com/img.jpg");
        image.setImagePrompt("New prompt");
        image.setSize(ImageSize.PORTRAIT);
        image.setSelected(false);

        assertThat(image.getImageUrl()).isEqualTo("https://new-url.com/img.jpg");
        assertThat(image.getImagePrompt()).isEqualTo("New prompt");
        assertThat(image.getSize()).isEqualTo(ImageSize.PORTRAIT);
        assertThat(image.getSelected()).isFalse();
    }

    @Test
    void relationship_post() {
        Post post = Post.builder().id(5L).build();
        PostImage image = PostImage.builder().post(post).build();

        assertThat(image.getPost()).isSameAs(post);
        assertThat(image.getPost().getId()).isEqualTo(5L);
    }

    @Test
    void nullFields_areHandled() {
        PostImage image = PostImage.builder().build();

        assertThat(image.getId()).isNull();
        assertThat(image.getImageUrl()).isNull();
        assertThat(image.getImagePrompt()).isNull();
        assertThat(image.getSize()).isNull();
        assertThat(image.getPost()).isNull();
    }

    @Test
    void selected_defaultIsTrue() {
        PostImage image = new PostImage();
        assertThat(image.getSelected()).isTrue();
    }

    @Test
    void size_enumValues() {
        PostImage square = PostImage.builder().size(ImageSize.SQUARE).build();
        PostImage landscape = PostImage.builder().size(ImageSize.LANDSCAPE).build();
        PostImage portrait = PostImage.builder().size(ImageSize.PORTRAIT).build();

        assertThat(square.getSize()).isEqualTo(ImageSize.SQUARE);
        assertThat(landscape.getSize()).isEqualTo(ImageSize.LANDSCAPE);
        assertThat(portrait.getSize()).isEqualTo(ImageSize.PORTRAIT);
    }
}
