package com.hackathon.templeback.user.annotations;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecurityAnnotations {

    @interface IsAdmin {
        @PreAuthorize("hasRole('ADMIN')")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface ForMethod {}
    }

    @interface IsUserOrAdmin {
        @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface ForMethod {}
    }

    @interface IsOwnerOrAdmin {
        @PreAuthorize("@userService.isOwner(#userId) or hasRole('ADMIN')")
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @interface ForMethod {}
    }
}
