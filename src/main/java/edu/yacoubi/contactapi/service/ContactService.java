package edu.yacoubi.contactapi.service;

import edu.yacoubi.contactapi.model.Contact;
import edu.yacoubi.contactapi.repository.ContactRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@SuppressWarnings("ALL")
@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepository.findAll(
                PageRequest.of(page, size, Sort.by("name"))
        );
    }

    public Contact getContact(String id) {
        return contactRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Contact not found")
        );
    }

    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact) {
        contactRepository.delete(contact);
    }
}