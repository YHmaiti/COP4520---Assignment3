        if (this.head.next.getReference() != this.tail) {
            System.out.println("From the constructor call of lock free list, head next doesn't point to tail...");
            System.exit(-1);
        }