package com.example.demo.service;

import com.example.demo.entity.Library;
import com.example.demo.repository.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibraryService {

    @Autowired
    private LibraryRepository libraryRepository;

    public List<Library> getAllLibraries() {
        return libraryRepository.findAll();
    }

    public Optional<Library> getLibraryById(Long id) {
        return libraryRepository.findById(id);
    }

    public Library saveLibrary(Library library) {
        return libraryRepository.save(library);
    }

    public void deleteLibrary(Long id) {
        libraryRepository.deleteById(id);
    }

    public Library getDefaultLibrary() {
        List<Library> libraries = libraryRepository.findAll();
        if (libraries.isEmpty()) {
            Library defaultLibrary = new Library("Thư viện trung tâm", "123 Đường ABC, TP.HCM", "0123456789");
            return libraryRepository.save(defaultLibrary);
        }
        return libraries.get(0);
    }
}
