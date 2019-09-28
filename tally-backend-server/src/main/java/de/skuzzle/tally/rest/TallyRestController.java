package de.skuzzle.tally.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.skuzzle.tally.rest.ratelimit.ApiRateLimiter;
import de.skuzzle.tally.rest.ratelimit.RateLimitExceededException;
import de.skuzzle.tally.service.TallyService;
import de.skuzzle.tally.service.TallySheet;
import de.skuzzle.tally.service.TallySheetNotAvailableException;

@RestController
public class TallyRestController {

    private final TallyService tallyService;
    private final ApiRateLimiter<HttpServletRequest> rateLimiter;

    TallyRestController(TallyService tallyService, ApiRateLimiter<HttpServletRequest> rateLimiter) {
        this.tallyService = tallyService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/public/{key}")
    public RestTallyResponse getTally(@PathVariable String key) {
        final TallySheet tallySheet = tallyService.getTallySheet(key);
        return RestTallyResponse.of(RestTallySheet.fromDomainObject(tallySheet));
    }

    @PostMapping("/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public RestTallyResponse createTally(@PathVariable @NotEmpty String name, HttpServletRequest request) {
        rateLimiter.blockIfRateLimitIsExceeded(request);
        final TallySheet tallySheet = tallyService.createNewTallySheet(name);
        return RestTallyResponse.of(RestTallySheet.fromDomainObject(tallySheet));
    }

    @PostMapping("/admin/{key}")
    @ResponseStatus(HttpStatus.OK)
    public RestTallyResponse increment(@PathVariable String key, @RequestBody @Valid RestTallyIncrement increment,
            HttpServletRequest request) {
        rateLimiter.blockIfRateLimitIsExceeded(request);
        final TallySheet tallySheet = tallyService.increment(key, increment.toDomainObject());
        return RestTallyResponse.of(RestTallySheet.fromDomainObject(tallySheet));
    }

    @DeleteMapping("/admin/{key}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTallySheet(@PathVariable String key, HttpServletRequest request) {
        rateLimiter.blockIfRateLimitIsExceeded(request);
        tallyService.deleteTallySheet(key);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RestTallyResponse> onRateLimitExceeded(RateLimitExceededException e) {
        final RestTallyResponse body = RestTallyResponse.failure(e.getMessage(), e.getClass().getName());
        return new ResponseEntity<>(body, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
    }

    @ExceptionHandler(TallySheetNotAvailableException.class)
    public ResponseEntity<RestTallyResponse> onTallySheetNotAvailable(TallySheetNotAvailableException e) {
        final RestTallyResponse body = RestTallyResponse.failure(e.getMessage(), e.getClass().getName());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

}
