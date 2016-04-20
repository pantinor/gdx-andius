/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.List;
import java.util.ListIterator;

public class ReverseListIterator implements ListIterator {

    private final List list;
    private ListIterator iterator;
    private boolean validForUpdate = true;

    public ReverseListIterator(List list) {
        super();
        this.list = list;
        iterator = list.listIterator(list.size());
    }

    @Override
    public boolean hasNext() {
        return iterator.hasPrevious();
    }

    @Override
    public Object next() {
        Object obj = iterator.previous();
        validForUpdate = true;
        return obj;
    }

    @Override
    public int nextIndex() {
        return iterator.previousIndex();
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasNext();
    }

    @Override
    public Object previous() {
        Object obj = iterator.next();
        validForUpdate = true;
        return obj;
    }

    @Override
    public int previousIndex() {
        return iterator.nextIndex();
    }

    @Override
    public void remove() {
        if (validForUpdate == false) {
            throw new IllegalStateException("Cannot remove from list until next() or previous() called");
        }
        iterator.remove();
    }

    @Override
    public void set(Object obj) {
        if (validForUpdate == false) {
            throw new IllegalStateException("Cannot set to list until next() or previous() called");
        }
        iterator.set(obj);
    }

    @Override
    public void add(Object obj) {
        if (validForUpdate == false) {
            throw new IllegalStateException("Cannot add to list until next() or previous() called");
        }
        validForUpdate = false;
        iterator.add(obj);
        iterator.previous();
    }

    public void reset() {
        iterator = list.listIterator(list.size());
    }

}
