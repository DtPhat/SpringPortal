package com.swd.uniportal.application.common;

import java.util.List;

public record FailedResponse(List<String> violations) {
}
