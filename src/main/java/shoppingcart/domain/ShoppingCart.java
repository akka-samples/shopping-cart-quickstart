package shoppingcart.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;


public record ShoppingCart(String cartId, List<LineItem> items, boolean checkedOut) { // <1>

  public record LineItem(String productId, String name, int quantity) { // <2>
    public LineItem withQuantity(int quantity) {
      return new LineItem(productId, name, quantity);
    }
  }

  public ShoppingCart onItemAdded(ShoppingCartEvent.ItemAdded itemAdded) {
    var item = itemAdded.item();
    var lineItem = updateItem(item); // <1>
    List<LineItem> lineItems = removeItemByProductId(item.productId()); // <2>
    lineItems.add(lineItem); // <3>
    lineItems.sort(Comparator.comparing(LineItem::productId));
    return new ShoppingCart(cartId, lineItems, checkedOut); // <4>
  }

  private LineItem updateItem(LineItem item) {
    return findItemByProductId(item.productId())
      .map(li -> li.withQuantity(li.quantity() + item.quantity()))
      .orElse(item);
  }

  private List<LineItem> removeItemByProductId(String productId) {
    return items().stream()
      .filter(lineItem -> !lineItem.productId().equals(productId))
      .collect(Collectors.toList());
  }

  public Optional<LineItem> findItemByProductId(String productId) {
    Predicate<LineItem> lineItemExists =
        lineItem -> lineItem.productId().equals(productId);
    return items.stream().filter(lineItemExists).findFirst();
  }


  public ShoppingCart onItemRemoved(ShoppingCartEvent.ItemRemoved itemRemoved) {
    List<LineItem> updatedItems =
        removeItemByProductId(itemRemoved.productId());
    updatedItems.sort(Comparator.comparing(LineItem::productId));
    return new ShoppingCart(cartId, updatedItems, checkedOut);
  }

  public ShoppingCart onCheckedOut() {
    return new ShoppingCart(cartId, items, true);
  }

}

