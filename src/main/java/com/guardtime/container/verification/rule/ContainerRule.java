package com.guardtime.container.verification.rule;

import com.guardtime.container.packaging.Container;

/**
 * Specific type of {@link Rule} which can only be used for a {@link Container} object.
 */
public interface ContainerRule extends Rule<Container> {
}
