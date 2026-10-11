# **INFO4V Reservas**

# 

# Sistema web interno para controle de reservas de vendas e estoque de produtos, desenvolvido com Play Framework 1.x.

# 

* # Como executar

# 

# Rode play run. Acesse `http://localhost:9000'.

# 

# | Perfil   | Login      | Senha      |

# |----------|------------|------------|

# | ADMIN    | `admin`    | `admin123` |

# | OPERADOR | `operador` | `op123`    |

# 

* # &#x20;Diagrama de classes

# 

# !\[Diagrama de classes](docs/diagramaReserva.png)

# 

* # &#x20;Regras de validação

# 

# \- Nome do cliente e produto são obrigatórios; a quantidade deve ser maior que zero.

# \- A quantidade reservada não pode passar do estoque disponível.

# \- Uma reserva retirada (ENTREGUE) não pode ser editada, e a retirada não devolve o estoque.

# \- Remover uma reserva ativa devolve o estoque; isso só pode ser feito pelo ADMIN.

# \- Produto: nome obrigatório, preço mínimo de R$ 0,01 e estoque não negativo.

# \- Produto com reservas ou vendas vinculadas não pode ser excluído.

# 

* # Segurança

# 

# Os controllers `Reservas` e `Application` usam `@With(Seguranca.class)`. Antes de cada ação, o interceptador confere se há usuário logado e, nas ações marcadas com `@Administrador`, se o perfil é ADMIN.

