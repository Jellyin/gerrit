import java.util.concurrent.atomic.AtomicInteger;
  private static AtomicInteger CHANGE_ID_COUNTER = new AtomicInteger();

  private static String nextChangeId() {
    // Tests use a variety of mechanisms for setting temporary timestamps, so we can't guarantee
    // that the PersonIdent (or any other field used by the Change-Id generator) for any two test
    // methods in the same acceptance test class are going to be different. But tests generally
    // assume that Change-Ids are unique unless otherwise specified. So, don't even bother trying to
    // reuse JGit's Change-Id generator, just do the simplest possible thing and convert a counter
    // to hex.
    return String.format("%040x", CHANGE_ID_COUNTER.incrementAndGet());
  }

      commitBuilder = testRepo.branch("HEAD").commit().insertChangeId(nextChangeId());