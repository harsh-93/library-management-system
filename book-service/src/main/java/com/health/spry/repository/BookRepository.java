package com.health.spry.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.health.spry.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
	Optional<Book> findByIdAndDeletedFalse(Long id);

//@Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE b.isbn = :isbn AND b.deleted = false")
//Boolean existsByIsbnAndNotDeleted(@Param("isbn") String isbn);

	Boolean existsByIsbnAndDeletedFalse(String isbn);

//@Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE b.isbn = :isbn AND b.id != :id AND b.deleted = false")
//Boolean existsByIsbnAndIdNotAndNotDeleted(@Param("isbn") String isbn, @Param("id") Long id);}

	Boolean existsByIsbnAndIdNotAndDeletedFalse(String isbn, Long id);

	boolean existsByIsbn(String isbn);
}
