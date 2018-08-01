package com.example.demo.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/*
    intermediate processor
    PersonItemProcessor implements Spring Batch’s ItemProcessor interface
    According to the interface, you receive an incoming Person object, after which you transform it to an upper-cased Person
 */
public class PersonItemProcessor implements ItemProcessor<Person, Person> {
    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    /*
        A simple transformer that converts the names to uppercase
     */
    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }
}
