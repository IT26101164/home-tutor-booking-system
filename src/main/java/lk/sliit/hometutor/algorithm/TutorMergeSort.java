package lk.sliit.hometutor.algorithm;

import java.util.*;
import lk.sliit.hometutor.model.Tutor;

/** Stable O(n log n) merge sort; O(n) auxiliary storage. No library sort here. */
public final class TutorMergeSort {
  private TutorMergeSort() {}

  public static List<Tutor> sort(List<Tutor> input) {
    List<Tutor> a = new ArrayList<>(input);
    List<Tutor> tmp = new ArrayList<>(a);
    sort(a, tmp, 0, a.size());
    return a;
  }

  private static void sort(List<Tutor> a, List<Tutor> t, int lo, int hi) {
    if (hi - lo < 2) return;
    int mid = (lo + hi) / 2;
    sort(a, t, lo, mid);
    sort(a, t, mid, hi);
    int i = lo, j = mid, k = lo;
    while (i < mid && j < hi) {
      if (compare(a.get(i), a.get(j)) <= 0) t.set(k++, a.get(i++));
      else t.set(k++, a.get(j++));
    }
    while (i < mid) t.set(k++, a.get(i++));
    while (j < hi) t.set(k++, a.get(j++));
    for (k = lo; k < hi; k++) a.set(k, t.get(k));
  }

  private static int compare(Tutor a, Tutor b) {
    int c = a.subject().compareToIgnoreCase(b.subject());
    return c != 0 ? c : Long.compare(a.id(), b.id());
  }
}
