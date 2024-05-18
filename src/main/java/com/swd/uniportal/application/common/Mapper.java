package com.swd.uniportal.application.common;

/**
 * Interface for Mapper.
 * @param <I>       Domain type.
 * @param <O>       Dto type.
 */
public interface Mapper<I, O> {

    O toDto(I domain);

    I toDomain(O dto);
}
