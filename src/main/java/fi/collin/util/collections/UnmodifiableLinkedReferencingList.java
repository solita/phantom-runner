package fi.collin.util.collections;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * <p>A list which can be used to provide a flattening abstraction layer between multiple
 * lists. UnmodifiableLinkedReferencingList uses internally {@link ArrayList} for saving the
 * sub lists thus providing random access speed between them. UnmodifiableLinkedReferencingList
 * cannot implement {@link RandomAccess} though since it needs some logic to decide between the
 * lists when elements are requested from it. Generally retrieval operations are handled with
 * good performance though and the speed of UnmodifiableLinkedReferencingList is more dependent
 * on the implementation of the sublists than UnmodifiableLinkedReferencingList itself.</p>
 * 
 * <p>UnmodifiableLinkedReferencingList is unmodifiable but it <b>does not guarantee</b> the
 * integrity of the list system it represents. If the child lists are modified directly during 
 * the lifetime of UnmodifiableLinkedReferencingList then it might provide unexpected results.
 * This is done due to speed: if the integrity were to be ensured then the sublist index would
 * have to be checked at each access and rebuilt if necessary. This would cause serious performance
 * problems in large datasets.</p>
 * 
 * <p>Use UnmodifiableLinkedReferencingList to provide an easy way to collect the data in multiple
 * different sources into one iterable list without having to copy the data from the original
 * lists.</p>
 * 
 * <p>Iteration via {@link Iterable} or {@link ListIterator} of UnmodifiableLinkedReferencingList 
 * generally performs better than using a for loop with growing indicies and {@link List#get(int)}.
 * This speed difference increases with the amount of sublists, with only one sublist the performance
 * is practically identical to the sublist implementation.</p>
 *
 * @author Niklas Collin
 */
public class UnmodifiableLinkedReferencingList<E> implements List<E> {

    private final ArrayList<List<E>> subLists;
    private final int[] sizeCache;
    private final int size;
    
    public UnmodifiableLinkedReferencingList(List<E> l1) {
        this(Arrays.asList(l1));
    }
    
    public UnmodifiableLinkedReferencingList(List<E> l1, List<E> l2) {
        this(Arrays.asList(l1, l2));
    }
    
    public UnmodifiableLinkedReferencingList(List<E> l1, List<E> l2, List<E> l3) {
        this(Arrays.asList(l1, l2, l3));
    }
    
    public UnmodifiableLinkedReferencingList(List<E> l1, List<E> l2, List<E> l3, List<E> l4) {
        this(Arrays.asList(l1, l2, l3, l4));
    }
    
    public UnmodifiableLinkedReferencingList(List<E> l1, List<E> l2, List<E> l3, List<E> l4, List<E> l5) {
        this(Arrays.asList(l1, l2, l3, l4, l5));
    }
     
    /**
     * No varargs version provided since it causes warnings. Most use cases should be covered with available
     * constructors though.
     */
    public UnmodifiableLinkedReferencingList(List<E>[] lists) {
        this(Arrays.asList(lists));
    }
    
    public UnmodifiableLinkedReferencingList(Iterable<List<E>> lists) {
        this.subLists = new ArrayList<List<E>>();

        for (List<E> list : lists) {
            subLists.add(unmodifiableList(list));
        }
        
        this.sizeCache = new int[subLists.size()];
        
        int size = 0;
        for (int i = 0; i < subLists.size(); i++) {
            this.sizeCache[i] = subLists.get(i).size();
            size += subLists.get(i).size();
        }
        this.size = size;
    }
    
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (List<E> list : subLists) {
            if (!list.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        for (List<E> list : subLists) {
            if (list.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int currentSize = 0;
        for (int i = 0; i < subLists.size(); i++) {
            List<E> list = subLists.get(i);
            Object[] copyArr = list.toArray();
            System.arraycopy(copyArr, 0, arr, currentSize, copyArr.length);
            currentSize += copyArr.length;
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
        }
        System.arraycopy(toArray(), 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        checkRange(index);
        return getSubList(index).get(getSubListIndex(index));
    }
    
    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        int indexPrepend = 0;
        
        for (List<E> list : subLists) {
            int index = list.indexOf(o);
            if (index != -1) {
                return indexPrepend + index;
            }
            indexPrepend += list.size();
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int indexPrepend = size;
        
        for (int i = subLists.size() - 1; i >= 0; i--) {
            List<E> list = subLists.get(i);
            indexPrepend -= list.size();
            
            int index = list.lastIndexOf(o);
            if (index != -1) {
                return indexPrepend + index;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ImmutableLinkedReferencingListIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new ImmutableLinkedReferencingListIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        checkRange(fromIndex);
        checkRange(toIndex);
        
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (int i = fromIndex; i < toIndex; i++) {
            builder.add(get(i));
        }
        return builder.build();
    }
    
    private void checkRange(int requestedIndex) {
        if (size <= requestedIndex) throw new IndexOutOfBoundsException(
                String.format("Index: %d, Size: %d", requestedIndex, size));
    }

    private List<E> getSubList(int index) {
        int newIndex = index;
        for (int i = 0; i < this.sizeCache.length; i++) {
            int cache = this.sizeCache[i];
            if (cache > newIndex) return subLists.get(i);
            newIndex -= cache;
        }
        if (subLists.isEmpty()) {
            return ImmutableList.of();
        }
        return Iterables.getLast(subLists);
    }
    
    private int getSubListIndex(int index) {
        int newIndex = index;
        for (int i : this.sizeCache) {
            if (i > newIndex) return newIndex;
            newIndex -= i;
        }
        return newIndex;
    }


    private class ImmutableLinkedReferencingListIterator implements ListIterator<E> {

        private int subListIndex = 0;
        private int wholeListIndex = 0;
        private ListIterator<E> subIterator;
        
        public ImmutableLinkedReferencingListIterator() {
            if (!subLists.isEmpty())
                subIterator = subLists.get(0).listIterator();
        }
        
        public ImmutableLinkedReferencingListIterator(int index) {
            this.subListIndex = getSubListIndex(index);
            this.subIterator = getSubList(subListIndex).listIterator();
        }
        
        @Override
        public boolean hasNext() {
        	if (subIterator == null) {
        		return false;
        	}
            if (subIterator.hasNext()) {
                return true;
            }
            subListIndex++;
            if (subListIndex >= subLists.size()) {
                return false;
            }
            subIterator = subLists.get(subListIndex).listIterator();
            return hasNext();
        }

        @Override
        public E next() {
            if (hasNext()) {
                wholeListIndex++;
                return subIterator.next();
            }
            return null;
        }

        @Override
        public boolean hasPrevious() {
            if (subIterator.hasPrevious()) {
                return true;
            }
            subListIndex--;
            if (subListIndex < 0) {
                return false;
            }
            subIterator = subLists.get(subListIndex).listIterator();
            return hasPrevious();
        }

        @Override
        public E previous() {
            if (hasPrevious()) {
                wholeListIndex--;
                return subIterator.previous();
            }
            return null;
        }

        @Override
        public int nextIndex() {
            return wholeListIndex;
        }

        @Override
        public int previousIndex() {
            return wholeListIndex - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public static <E> Builder<E> builder() {
        return new Builder<E>();
    }
    
    public static class Builder<E> {
        private List<List<E>> lists = new ArrayList<List<E>>();
        
        public Builder() {}
        
        public Builder<E> add(List<E> list) {
            lists.add(list);
            return this;
        }
        
        public UnmodifiableLinkedReferencingList<E> build() {
            return new UnmodifiableLinkedReferencingList<E>(lists);
        }
    }

}

