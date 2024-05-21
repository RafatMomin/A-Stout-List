package edu.iastate.cs228.hw3;

import java.util.*;

/**
 * Implementation of the list interface based on linked nodes that store
 * multiple items per node. Rules for adding and removing elements ensure that
 * each node (except possibly the last one) is at least half full.
 *
 * @author Rafat Momin
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E> {
	/**
	 * Default number of elements that may be stored in each node.
	 */
	private static final int DEFAULT_NODESIZE = 4;

	/**
	 * Number of elements that can be stored in each node.
	 */
	private final int nodeSize;

	/**
	 * Dummy node for head. It should be private but set to public here only for
	 * grading purpose. In practice, you should always make the head of a linked
	 * list a private instance variable.
	 */
	public Node head;

	/**
	 * Dummy node for tail.
	 */
	private Node tail;

	/**
	 * Number of elements in the list.
	 */
	private int size;

	/**
	 * Constructs an empty list with the default node size.
	 */
	public StoutList() {
		this(DEFAULT_NODESIZE);
	}

	/**
	 * Constructs an empty list with the given node size.
	 * 
	 * @param nodeSize number of elements that may be stored in each node, must be
	 *                 an even number
	 */
	public StoutList(int nodeSize) {
		if (nodeSize <= 0 || nodeSize % 2 != 0)
			throw new IllegalArgumentException();

		// dummy nodes
		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		this.nodeSize = nodeSize;
	}

	/**
	 * Constructor for grading only. Fully implemented.
	 * 
	 * @param head
	 * @param tail
	 * @param nodeSize i am a bitch
	 * @param size
	 */
	public StoutList(Node head, Node tail, int nodeSize, int size) {
		this.head = head;
		this.tail = tail;
		this.nodeSize = nodeSize;
		this.size = size;
	}

	/**
	 * @return the number of elements in the list
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * Adds the item to the end of the list
	 * 
	 * @param item the item to add to the list
	 * @return true, if item was added successfully
	 * 		   false, if item already exists in the list
	 */
	@Override
	public boolean add(E item) {
		if (item == null) {
			throw new NullPointerException();
		}
		
		if(contains(item))
			return false;

		if (size == 0) {
			Node newNode = new Node();
			newNode.addItem(item);
			head.next = newNode;
			newNode.previous = head;
			newNode.next = tail;
			tail.previous = newNode;
		} else {
			if (tail.previous.count < nodeSize) {
				tail.previous.addItem(item);
			}
			else {
				Node newNode = new Node();
				newNode.addItem(item);
				Node temp = tail.previous;
				temp.next = newNode;
				newNode.previous = temp;
				newNode.next = tail;
				tail.previous = newNode;
			}
		}
		size++;
		return true;
	}

	/**
	 * A simple contains method to search through list to check for a duplicate before adding
	 *
	 * @param item the item to search for
	 * @return whether the list contains the item or not
	 */
	public boolean contains(E item) {
		if(size < 1)
			return false;
		Node temp = head.next;
		while(temp != tail) {
			for(int i=0;i<temp.count;i++) {
				if(temp.data[i].equals(item))
					return true;
				temp = temp.next;
			}
		}
		return false;
	}

	/**
	 * Adds item to a specific index
	 * Basically followed the guideline from the project description
	 *
	 * @param pos the position where the item should go to
	 * @param item the item to add to the list
	 */
	@Override
	public void add(int pos, E item) {
		if (pos < 0 || pos > size)
			throw new IndexOutOfBoundsException();

		if (head.next == tail)
			add(item);

		NodeOffsetLocation nodeOffsetLocation = find(pos);
        assert nodeOffsetLocation != null;
        Node temp = nodeOffsetLocation.node;
		int offset = nodeOffsetLocation.offset;

		if (offset == 0) {
			if (temp.previous.count < nodeSize && temp.previous != head) {
				temp.previous.addItem(item);
				size++;
				return;
			}
			else if (temp == tail) {
				add(item);
				size++;
				return;
			}
		}
		if (temp.count < nodeSize) {
			temp.addItem(offset, item);
		}
		else {
			Node successor = new Node();
			int halfPoint = nodeSize / 2;
			int count = 0;
			while (count < halfPoint) {
				successor.addItem(temp.data[halfPoint]);
				temp.removeItem(halfPoint);
				count++;
			}

			Node previousSuccessor = temp.next;

			temp.next = successor;
			successor.previous = temp;
			successor.next = previousSuccessor;
			previousSuccessor.previous = successor;

			if (offset <= nodeSize / 2) {
				temp.addItem(offset, item);
			}
			if (offset > nodeSize / 2) {
				successor.addItem((offset - nodeSize / 2), item);
			}

		}
		size++;
	}

	/**
	 * Removes an item that is in a specific index
	 * Basically followed the guideline from the project description
	 * 
	 * @param pos the position where the item should be removed
	 * @return item that was removed
	 */
	@Override
	public E remove(int pos) {
		if (pos < 0 || pos > size)
			throw new IndexOutOfBoundsException();
		NodeOffsetLocation nodeOffsetLocation = find(pos);
        assert nodeOffsetLocation != null;
        Node temp = nodeOffsetLocation.node;
		int offset = nodeOffsetLocation.offset;
		E nodeValue = temp.data[offset];

		if (temp.next == tail && temp.count == 1) {
			Node predecessor = temp.previous;
			predecessor.next = temp.next;
			temp.next.previous = predecessor;
			temp = null;
		}
		else if (temp.next == tail || temp.count > nodeSize / 2) {
			temp.removeItem(offset);
		}
		else {
			temp.removeItem(offset);
			Node succesor = temp.next;

			if (succesor.count > nodeSize / 2) {
				temp.addItem(succesor.data[0]);
				succesor.removeItem(0);
			}
			else if (succesor.count <= nodeSize / 2) {
				for (int i = 0; i < succesor.count; i++) {
					temp.addItem(succesor.data[i]);
				}
				temp.next = succesor.next;
				succesor.next.previous = temp;
				succesor = null;
			}
		}
		size--;
		return nodeValue;
	}

	/**
	 * Sort all elements in the stout list in the NON-DECREASING order. You may do
	 * the following. Traverse the list and copy its elements into an array,
	 * deleting every visited node along the way. Then, sort the array by calling
	 * the insertionSort() method. (Note that sorting efficiency is not a concern
	 * for this project.) Finally, copy all elements from the array back to the
	 * stout list, creating new nodes for storage. After sorting, all nodes but
	 * (possibly) the last one must be full of elements.
	 * 
	 * Comparator<E> must have been implemented for calling insertionSort().
	 */
	public void sort() {
		E[] sortedDataList = (E[]) new Comparable[size];

		int tempIndex = 0;
		Node temp = head.next;
		while (temp != tail) {
			for (int i = 0; i < temp.count; i++) {
				sortedDataList[tempIndex] = temp.data[i];
				tempIndex++;
			}
			temp = temp.next;
		}

		head.next = tail;
		tail.previous = head;

		insertionSort(sortedDataList, new ElementComparator());
		size = 0;
        this.addAll(Arrays.asList(sortedDataList));

	}

	/**
	 * Sort all elements in the stout list in the NON-INCREASING order. Call the
	 * bubbleSort() method. After sorting, all but (possibly) the last nodes must be
	 * filled with elements.
	 * 
	 * Comparable<? super E> must be implemented for calling bubbleSort().
	 */
	public void sortReverse() {
		E[] reverseSortedDataList = (E[]) new Comparable[size];

		int tempIndex = 0;
		Node temp = head.next;
		while (temp != tail) {
			for (int i = 0; i < temp.count; i++) {
				reverseSortedDataList[tempIndex] = temp.data[i];
				tempIndex++;
			}
			temp = temp.next;
		}

		head.next = tail;
		tail.previous = head;

		bubbleSort(reverseSortedDataList);
		size = 0;
        this.addAll(Arrays.asList(reverseSortedDataList));
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return new StoutListIterator(index);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes.
	 */
	public String toStringInternal() {
		return toStringInternal(null);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes and the position of the iterator.
	 *
	 * @param iter an iterator for this list
	 */
	public String toStringInternal(ListIterator<E> iter) {
		int count = 0;
		int location = -1;
		if (iter != null) {
			location = iter.nextIndex();
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append('[');
		Node currentNode = head.next;
		while (currentNode != tail) {
			stringBuilder.append('(');
			E data = currentNode.data[0];
			if (data == null) {
				stringBuilder.append("-");
			} else {
				if (location == count) {
					stringBuilder.append("| ");
					location = -1;
				}
				stringBuilder.append(data.toString());
				++count;
			}

			for (int i = 1; i < nodeSize; ++i) {
				stringBuilder.append(", ");
				data = currentNode.data[i];
				if (data == null) {
					stringBuilder.append("-");
				} else {
					if (location == count) {
						stringBuilder.append("| ");
						location = -1;
					}
					stringBuilder.append(data.toString());
					++count;

					// iterator at end
					if (location == size && count == size) {
						stringBuilder.append(" |");
						location = -1;
					}
				}
			}
			stringBuilder.append(')');
			currentNode = currentNode.next;
			if (currentNode != tail)
				stringBuilder.append(", ");
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}

	/**
	 * Node type for this list. Each node holds a maximum of nodeSize elements in an
	 * array. Empty slots are null.
	 */
	private class Node {
		/**
		 * Array of actual data elements.
		 */
		// Unchecked warning unavoidable.
		public E[] data = (E[]) new Comparable[nodeSize];

		/**
		 * Link to next node.
		 */
		public Node next;

		/**
		 * Link to previous node;
		 */
		public Node previous;

		/**
		 * Index of the next available offset in this node, also equal to the number of
		 * elements in this node.
		 */
		public int count;

		/**
		 * Adds an item to this node at the first available offset. Precondition: count
		 * < nodeSize
		 * 
		 * @param item element to be added
		 */
		void addItem(E item) {
			if (count >= nodeSize) {
				return;
			}
			data[count++] = item;
		}

		/**
		 * Adds an item to this node at the indicated offset, shifting elements to the
		 * right as necessary.
		 * 
		 * Precondition: count < nodeSize
		 * 
		 * @param offset array index at which to put the new element
		 * @param item   element to be added
		 */
		void addItem(int offset, E item) {
			if (count >= nodeSize) {
				return;
			}
			for (int i = count - 1; i >= offset; --i) {
				data[i + 1] = data[i];
			}
			++count;
			data[offset] = item;
		}

		/**
		 * Deletes an element from this node at the indicated offset, shifting elements
		 * left as necessary. Precondition: 0 <= offset < count
		 * 
		 * @param offset
		 */
		void removeItem(int offset) {
			E item = data[offset];
			for (int i = offset + 1; i < nodeSize; ++i) {
				data[i - 1] = data[i];
			}
			data[count - 1] = null;
			--count;
		}
	}

	/**
	 * Helper class to represent a specific point of the list
	 */
	private class NodeOffsetLocation {
		public Node node;
		public int offset;

		public NodeOffsetLocation(Node node, int offset) {
			this.node = node;
			this.offset = offset;
		}
	}

	/**
	 * Helper method to locate an specific item
	 * 
	 * @param pos position of item that needs a info
	 * @return NodeInfo of specific point of the list
	 */
	private NodeOffsetLocation find(int pos) {
		Node temp = head.next;
		int location = 0;
		while (temp != tail) {
			if (location + temp.count <= pos) {
				location += temp.count;
				temp = temp.next;
				continue;
			}

            return new NodeOffsetLocation(temp, pos - location);

		}
		return null;
	}

	/**
	 * Custom Iterator for StoutList
	 */
	private class StoutListIterator implements ListIterator<E> {

		final int PREVIOUS_LAST_ACTION = 0;
		final int NEXT_LAST_ACTION = 1;

		/**
		 * pointer of iterator
		 */
		int currentLocation;
		
		/**
		 * data structure of iterator in array form
		 */
		public E[] dataList;
		
		/**
		 * tracks the lastAction taken by the program
		 * it is mainly used for remove() and set() method to determine
		 * which item to remove or set
		 */
		int lastAction;

		/**
		 * Default constructor
		 * Sets the pointer of iterator to the beginning of the list
		 */
		public StoutListIterator() {
			currentLocation = 0;
			lastAction = -1;
			setup();
		}

		/**
		 * Constructor finds node at a given position.
		 * Sets the pointer of iterator to the specific index of the list
		 * 
		 * @param pos
		 */
		public StoutListIterator(int pos) {
			currentLocation = pos;
			lastAction = -1;
			setup();
		}

		/**
		 * Takes the StoutList and put its data into dataList in an array form
		 */
		private void setup() {
			dataList = (E[]) new Comparable[size];

			int tempIndex = 0;
			Node temp = head.next;
			while (temp != tail) {
				for (int i = 0; i < temp.count; i++) {
					dataList[tempIndex] = temp.data[i];
					tempIndex++;
				}
				temp = temp.next;
			}
		}

		/**
		 * @return whether iterator has next available value or not
		 */
		@Override
		public boolean hasNext() {
			if (currentLocation >= size)
				return false;
			else
				return true;
		}

		/**
		 * Returns the next ready value and shifts the pointer by 1
		 * 
		 * @return the next ready value of the iterator
		 */
		@Override
		public E next() {
			if (!hasNext())
				throw new NoSuchElementException();
			lastAction = NEXT_LAST_ACTION;
			return dataList[currentLocation++];
		}

		/**
		 * Removes from the list the last element returned by next() or previous().
		 * Can only be called once per call of next() or previous()
		 * Also removes the element from the StoutList
		 */
		@Override
		public void remove() {
			if (lastAction == NEXT_LAST_ACTION) {
				StoutList.this.remove(currentLocation - 1);
				setup();
				lastAction = -1;
				currentLocation--;
				if (currentLocation < 0)
					currentLocation = 0;
			} else if (lastAction == PREVIOUS_LAST_ACTION) {
				StoutList.this.remove(currentLocation);
				setup();
				lastAction = -1;
			} else {
				throw new IllegalStateException();
			}
		}

		/**
		 * @return whether iterator has previous available value or not
		 */
		@Override
		public boolean hasPrevious() {
            return currentLocation > 0;
		}

		/**
		 * @return index of next available element
		 */
		@Override
		public int nextIndex() {
			return currentLocation;
		}
		
		/**
		 * Returns previous available element and shifts pointer by -1
		 * 
		 * @return previous available element
		 */
		@Override
		public E previous() {
			if (!hasPrevious())
				throw new NoSuchElementException();
			lastAction = PREVIOUS_LAST_ACTION;
			currentLocation--;
			return dataList[currentLocation];
		}

		/**
		 * @return index of previous element
		 */
		@Override
		public int previousIndex() {
			return currentLocation - 1;
		}

		/**
		 * Replaces the element at the current pointer
		 * 
		 * @param arg0 replacing element
		 */
		@Override
		public void set(E arg0) {
			if (lastAction == NEXT_LAST_ACTION) {
				NodeOffsetLocation nodeOffsetLocation = find(currentLocation - 1);
                assert nodeOffsetLocation != null;
                nodeOffsetLocation.node.data[nodeOffsetLocation.offset] = arg0;
				dataList[currentLocation - 1] = arg0;
			} else if (lastAction == PREVIOUS_LAST_ACTION) {
				NodeOffsetLocation nodeOffsetLocation = find(currentLocation);
                assert nodeOffsetLocation != null;
                nodeOffsetLocation.node.data[nodeOffsetLocation.offset] = arg0;
				dataList[currentLocation] = arg0;
			} else {
				throw new IllegalStateException();
			}

		}

		/**
		 * Adds an element to the end of the list
		 * 
		 * @param e adding element
		 */
		@Override
		public void add(E e) {
			if (e == null)
				throw new NullPointerException();

			StoutList.this.add(currentLocation, e);
			currentLocation++;
			setup();
			lastAction = -1;

		}

	}

	/**
	 * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING
	 * order.
	 * 
	 * @param arr  array storing elements from the list
	 * @param comp comparator used in sorting
	 */
	private void insertionSort(E[] arr, Comparator<? super E> comp) {
		for (int i = 1; i < arr.length; i++) {
			E key = arr[i];
			int j = i - 1;

			while (j >= 0 && comp.compare(arr[j], key) > 0) {
				arr[j + 1] = arr[j];
				j--;
			}
			arr[j + 1] = key;
		}
	}

	/**
	 * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a
	 * description of bubble sort please refer to Section 6.1 in the project
	 * description. You must use the compareTo() method from an implementation of
	 * the Comparable interface by the class E or ? super E.
	 * 
	 * @param arr array holding elements from the list
	 */
	private void bubbleSort(E[] arr) {
		int n = arr.length;
		for (int i = 0; i < n - 1; i++) {
			for (int j = 0; j < n - i - 1; j++) {
				if (arr[j].compareTo(arr[j + 1]) < 0) {
					E temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
				}
			}
		}

	}

	/**
	 * Custom Comparator to be used by insertion sort.
	 */
	class ElementComparator<E extends Comparable<E>> implements Comparator<E> {
		@Override
		public int compare(E e1, E e2) {
			return e1.compareTo(e2);
		}

	}

}