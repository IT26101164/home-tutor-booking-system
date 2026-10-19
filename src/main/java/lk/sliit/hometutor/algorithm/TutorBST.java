package lk.sliit.hometutor.algorithm;

import java.util.*;
import lk.sliit.hometutor.model.Tutor;

/** Unbalanced BST keyed by tutor ID. Iterative traversal avoids recursion overflow. */
public final class TutorBST {
  private static final class Node {
    Tutor value;
    Node left, right;

    Node(Tutor value) {
      this.value = value;
    }
  }

  private Node root;

  public void insert(Tutor tutor) {
    if (root == null) {
      root = new Node(tutor);
      return;
    }
    Node n = root;
    while (true) {
      if (tutor.id() == n.value.id()) {
        n.value = tutor;
        return;
      }
      if (tutor.id() < n.value.id()) {
        if (n.left == null) {
          n.left = new Node(tutor);
          return;
        }
        n = n.left;
      } else {
        if (n.right == null) {
          n.right = new Node(tutor);
          return;
        }
        n = n.right;
      }
    }
  }

  public Tutor search(long id) {
    Node n = root;
    while (n != null) {
      if (id == n.value.id()) return n.value;
      n = id < n.value.id() ? n.left : n.right;
    }
    return null;
  }

  public boolean delete(long id) {
    Node p = null, n = root;
    while (n != null && n.value.id() != id) {
      p = n;
      n = id < n.value.id() ? n.left : n.right;
    }
    if (n == null) return false;
    if (n.left != null && n.right != null) {
      Node sp = n, s = n.right;
      while (s.left != null) {
        sp = s;
        s = s.left;
      }
      n.value = s.value;
      p = sp;
      n = s;
    }
    Node child = n.left != null ? n.left : n.right;
    if (p == null) root = child;
    else if (p.left == n) p.left = child;
    else p.right = child;
    return true;
  }

  public List<Tutor> inOrder() {
    List<Tutor> out = new ArrayList<>();
    Deque<Node> stack = new ArrayDeque<>();
    Node n = root;
    while (n != null || !stack.isEmpty()) {
      while (n != null) {
        stack.push(n);
        n = n.left;
      }
      n = stack.pop();
      out.add(n.value);
      n = n.right;
    }
    return out;
  }

  /** Rebuild from persisted records around medians to avoid sorted-startup degeneration. */
  public static TutorBST from(Collection<Tutor> tutors) {
    TutorBST tree = new TutorBST();
    List<Tutor> a = new ArrayList<>(tutors);
    a.sort(Comparator.comparingLong(Tutor::id));
    tree.root = balanced(a, 0, a.size());
    return tree;
  }

  private static Node balanced(List<Tutor> a, int lo, int hi) {
    if (lo >= hi) return null;
    int m = (lo + hi) / 2;
    Node n = new Node(a.get(m));
    n.left = balanced(a, lo, m);
    n.right = balanced(a, m + 1, hi);
    return n;
  }
}
