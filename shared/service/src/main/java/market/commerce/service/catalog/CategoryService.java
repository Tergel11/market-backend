package market.commerce.service.catalog;

import lombok.RequiredArgsConstructor;
import market.commerce.LocalStringUtil;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.model.catalog.Category;
import market.commerce.model.enums.Status;
import market.commerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains the category tree. {@code path} and {@code level} are recomputed on
 * every save so subtree queries stay a single index lookup.
 *
 * @author Tergel
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public List<Category> children(String parentId) {
        return categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }

    /** The category itself plus everything beneath it. */
    public List<String> subtreeIds(String categoryId) {
        List<String> ids = new ArrayList<>();
        ids.add(categoryId);
        categoryRepository.findByPathContainingAndStatus(categoryId, Status.ACTIVE)
                .forEach(category -> ids.add(category.getId()));
        return ids;
    }

    public Category save(Category category) {
        if (LocalStringUtil.isEmpty(category.getSlug()))
            category.setSlug(LocalStringUtil.toSlug(category.getName().get(
                    market.commerce.model.enums.Locale.EN)));

        applyPath(category);
        return categoryRepository.save(category);
    }

    /**
     * Copies the parent's path and appends the parent id.
     */
    private void applyPath(Category category) {
        if (LocalStringUtil.isEmpty(category.getParentId())) {
            category.setPath(List.of());
            category.setLevel(0);
            return;
        }

        Category parent = findById(category.getParentId());
        if (parent.getId().equals(category.getId()))
            throw new MessageException("error.invalid-request");

        List<String> path = new ArrayList<>(
                parent.getPath() == null ? List.of() : parent.getPath());
        path.add(parent.getId());

        category.setPath(path);
        category.setLevel(path.size());
    }
}
