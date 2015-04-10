package info.interactivesystems.gamificationengine.dao;

import java.util.List;

import javax.persistence.Query;
import javax.validation.constraints.NotNull;

public class QueryUtils {

	public static @NotNull List configureQuery(Query query, int id, String apiKey) {
		query.setParameter("apiKey", apiKey);
		query.setParameter("id", id);

		List list = query.setMaxResults(1).getResultList();
		return list;
	}
}
