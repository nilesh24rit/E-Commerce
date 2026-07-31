package com.commercex.mapper;

import com.commercex.dto.ReviewResponse;
import com.commercex.entity.Product;
import com.commercex.entity.Review;
import com.commercex.entity.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-31T14:37:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public ReviewResponse toDto(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewResponse.ReviewResponseBuilder reviewResponse = ReviewResponse.builder();

        reviewResponse.productId( reviewProductId( review ) );
        reviewResponse.userId( reviewUserId( review ) );
        reviewResponse.id( review.getId() );
        reviewResponse.rating( review.getRating() );
        reviewResponse.title( review.getTitle() );
        reviewResponse.comment( review.getComment() );
        reviewResponse.verifiedPurchase( review.isVerifiedPurchase() );
        reviewResponse.createdAt( review.getCreatedAt() );
        reviewResponse.updatedAt( review.getUpdatedAt() );

        reviewResponse.userName( review.getUser().getFirstName() + " " + review.getUser().getLastName() );

        return reviewResponse.build();
    }

    private UUID reviewProductId(Review review) {
        if ( review == null ) {
            return null;
        }
        Product product = review.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private UUID reviewUserId(Review review) {
        if ( review == null ) {
            return null;
        }
        User user = review.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
